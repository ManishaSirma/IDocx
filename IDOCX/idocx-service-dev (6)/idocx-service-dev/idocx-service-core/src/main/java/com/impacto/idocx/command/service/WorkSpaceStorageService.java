package com.impacto.idocx.command.service;

import com.impacto.idocx.command.common.Constants;
import com.impacto.idocx.command.dao.FileMetadataRepository;
import com.impacto.idocx.command.dao.FolderMetadataRepository;
import com.impacto.idocx.command.dtos.FileMetaDataDto;
import com.impacto.idocx.command.dtos.FolderMetadataDto;
import com.impacto.idocx.command.dtos.FolderRequestDto;
import com.impacto.idocx.command.entity.FileMetadata;
import com.impacto.idocx.command.entity.FolderMetadata;
import com.impacto.idocx.command.exceptions.DirectoryCreationException;
import com.impacto.idocx.command.exceptions.ErrorCode;
import com.impacto.idocx.command.exceptions.FailedToUpdateResourcesException;
import com.impacto.idocx.command.exceptions.FileNotFoundException;
import com.impacto.idocx.command.exceptions.FileReadingException;
import com.impacto.idocx.command.exceptions.FileStorageException;
import com.impacto.idocx.command.exceptions.FolderAlreadyExistsException;
import com.impacto.idocx.command.exceptions.ResourceNotFoundException;
import com.impacto.idocx.command.model.FileUploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class WorkSpaceStorageService {

    private final Path rootLocation;
    private final FileMetadataRepository fileMetadataRepository;
    private final FolderMetadataRepository folderMetadatarepository;
    private final ModelMapper modelMapper;

    @Autowired
    public WorkSpaceStorageService(Environment environment, FileMetadataRepository fileMetadataRepository, FolderMetadataRepository folderMetadatarepository, ModelMapper modelMapper) {
        this.rootLocation = Paths.get(environment.getProperty("file.storage.location"));
        this.fileMetadataRepository = fileMetadataRepository;
        this.folderMetadatarepository = folderMetadatarepository;
        this.modelMapper = modelMapper;
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            log.info("Throwing error while initializing the storage: . Error: {}", ErrorCode.STORAGE_INITIALIZATION_EXCEPTION);
            throw new DirectoryCreationException(ErrorCode.STORAGE_INITIALIZATION_EXCEPTION, "Could not initialize storage");
        }
    }

    public void createDirectory(String directoryName) {
        try {
            Path dir = rootLocation.resolve(directoryName);
            Files.createDirectories(dir);
        } catch (IOException e) {
            log.info("Could not create directory: . Error: {}", ErrorCode.DIRECTORY_EXCEPTION);
            throw new DirectoryCreationException(ErrorCode.DIRECTORY_EXCEPTION, "Could not create directory");
        }
    }

    public void store(FileUploadRequest fileUploadRequest) {
        String filePath = fileUploadRequest.getFilePath();
        createDirectory(filePath);
        for (MultipartFile file : fileUploadRequest.getFiles()) {
            if (file.isEmpty()) {
                log.info("File not found in the request. Error: {}", ErrorCode.FILE_NOT_FOUND_EXCEPTION);
                throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND_EXCEPTION, "There is no files in the request");
            }
            try {
                Path destinationFile = getDestinationFile(filePath, file);
                Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                String directoryName = destinationFile.toString().replace("\\", "/");
                processFileMetadata(fileUploadRequest, file, directoryName);
            } catch (IOException e) {
                throw new FileStorageException(ErrorCode.FILE_STORAGE_EXCEPTION, "Failed to store file.");
            }
        }
    }

    private void processFileMetadata(FileUploadRequest fileUploadRequest, MultipartFile file, String directoryName) {
        String fileName = file.getOriginalFilename();
        fileMetadataRepository.findByFileNameAndFilePath(fileName, fileUploadRequest.getFilePath()).ifPresentOrElse(metadata -> {
            if (!directoryName.equals(metadata.getDirectoryName())) {
                metadata.setDirectoryName(directoryName);
                fileMetadataRepository.save(metadata);
            }
        }, () -> fileMetadataRepository.save(createFileMetaData(fileUploadRequest, file, directoryName)));

        folderMetadatarepository.findByFolderPath(fileUploadRequest.getFilePath()).ifPresentOrElse(folderMetadata -> {
            if (folderMetadata.getDocuments() == null)
                folderMetadata.setDocuments(new ArrayList<>());
            boolean documentExists = folderMetadata.getDocuments().stream()
                    .anyMatch(doc -> fileName.equals(doc.getFileName()));
            if (!documentExists) {
                folderMetadata.getDocuments().add(createFileMetaData(fileUploadRequest, file, directoryName));
                folderMetadatarepository.save(folderMetadata);
            }
        }, () -> folderMetadatarepository.save(createFolder(fileUploadRequest, Arrays.asList(createFileMetaData(fileUploadRequest, file, directoryName)))));
    }

    private FileMetadata createFileMetaData(FileUploadRequest fileUploadRequest, MultipartFile file, String directoryName) {
        FileMetadata metadata = new FileMetadata(file.getOriginalFilename(), fileUploadRequest.getFilePath(), directoryName);
        metadata.setExtension(createExtension(file.getOriginalFilename()));
        metadata.setTag(fileUploadRequest.getTag());
        metadata.setVersion(fileUploadRequest.getVersion());
        metadata.setRemarks(fileUploadRequest.getRemarks());
        metadata.setWorkSpaceType(Constants.WORKSPACE_TYPE.valueOf(fileUploadRequest.getWorkspaceType().toUpperCase()));
        metadata.setPasswordProtected(fileUploadRequest.isPasswordProtected());
        return metadata;
    }

    private static FolderMetadata createFolder(FileUploadRequest fileUploadRequest, List<FileMetadata> fileMetadataList) {
        FolderMetadata folderMetadata = new FolderMetadata();
        folderMetadata.setFolderName(fileUploadRequest.getFolderName());
        folderMetadata.setFolderPath(fileUploadRequest.getFilePath());
        folderMetadata.setWorkSpaceType(Constants.WORKSPACE_TYPE.valueOf(fileUploadRequest.getWorkspaceType().toUpperCase()));
        folderMetadata.setDocuments(fileMetadataList);
        return folderMetadata;
    }

    public String createExtension(String originalFilename) {
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            return originalFilename.substring(dotIndex);
        } else {
            return "";
        }
    }

    public Resource loadFileAsResource(String documentId) {
        try {
            FileMetadata fileMetadata = this.fileMetadataRepository.findById(documentId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND_EXCEPTION, "File with id: " + documentId + " not found"));

            Path filePath = Paths.get(fileMetadata.getDirectoryName());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileReadingException(ErrorCode.FILE_READING_EXCEPTION, "Could not read file: " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new FileReadingException(ErrorCode.FILE_READING_EXCEPTION, "Error while reading file: ");
        }
    }

    public Path getDestinationFile(String directoryName, MultipartFile file) {
        return this.rootLocation.resolve(directoryName)
                .resolve(Paths.get(file.getOriginalFilename()))
                .normalize().toAbsolutePath();
    }

    public List<FolderMetadataDto> getAllFolderStructure(String workSpaceName) {
        List<FolderMetadata> allByWorkSpaceType = folderMetadatarepository.findByWorkSpaceTypeAndIsTrashFalseAndIsArchiveFalse(workSpaceName.toUpperCase());
        return allByWorkSpaceType.stream()
                .map(folderMetadata -> modelMapper.map(folderMetadata, FolderMetadataDto.class))
                .toList();
    }

    public Page<FileMetaDataDto> getFilesMetadata(String filePath, int pageNo, int pageSize) {
        return fileMetadataRepository.findAllByFilePathAndIsTrashFalseAndIsArchiveFalse(filePath, PageRequest.of(pageNo, pageSize))
                .map(fileMetadata -> convertToDto(fileMetadata, FileMetaDataDto.class));
    }

    private <S, T> T convertToDto(S source, Class<T> targetClass) {
        return modelMapper.map(source, targetClass);
    }

    public FolderMetadataDto updateFolderName(String id, String newFolderName) {
        FolderMetadata folderMetadata = folderMetadatarepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND_EXCEPTION, "Folder with id: " + id + " not found"));
        updateDirectory(folderMetadata, newFolderName);
        updateRelatedMetadata(folderMetadata, newFolderName);
        folderMetadata.setFolderName(newFolderName);
        return convertToDto(folderMetadata, FolderMetadataDto.class);
    }

    private void updateDirectory(FolderMetadata folderMetadata, String newFolderName) {
        String oldPath = folderMetadata.getFolderPath();
        String newPath = constructNewPath(oldPath, newFolderName);
        try {
            Files.move(rootLocation.resolve(oldPath), rootLocation.resolve(newPath));
        } catch (IOException e) {
            throw new FailedToUpdateResourcesException(ErrorCode.FAILED_TO_UPDATE_FILE_EXCEPTION,
                    "Failed to rename directory from " + oldPath + " to " + newPath);
        }
        folderMetadata.setFolderPath(newPath);
    }

    private String constructNewPath(String oldPath, String newFolderName) {
        return oldPath.substring(0, oldPath.lastIndexOf("/") + 1) + newFolderName;
    }

    private void updateRelatedMetadata(FolderMetadata folderMetadata, String newFolderName) {
        if (folderMetadata.getDocuments() != null) {
            List<FileMetadata> fileMetadatas = fileMetadataRepository.findAllByWorkSpaceType(String.valueOf(folderMetadata.getWorkSpaceType()));
            for (FileMetadata metadata : fileMetadatas) {
                if (metadata.getFilePath().contains(folderMetadata.getFolderName())) {
                    updateFileMetadata(metadata, folderMetadata.getFolderName(), newFolderName);
                }
            }
            fileMetadataRepository.saveAll(fileMetadatas);
        }

        List<FolderMetadata> folderMetadatas = folderMetadatarepository.findAllByWorkSpaceType(String.valueOf(folderMetadata.getWorkSpaceType()));
        for (FolderMetadata metadata : folderMetadatas) {
            if (metadata.getFolderPath().contains(folderMetadata.getFolderName())) {
                updateFolderMetadata(metadata, folderMetadata.getFolderName(), newFolderName);
            }
        }
        folderMetadatarepository.saveAll(folderMetadatas);
    }

    private void updateFileMetadata(FileMetadata metadata, String oldName, String newName) {
        metadata.setFilePath(metadata.getFilePath().replace(oldName, newName));
        metadata.setDirectoryName(metadata.getDirectoryName().replace(oldName, newName));
    }

    private void updateFolderMetadata(FolderMetadata metadata, String oldName, String newName) {
        metadata.setFolderPath(metadata.getFolderPath().replace(oldName, newName));
        if (metadata.getDocuments() != null)
            metadata.getDocuments().forEach(doc -> updateFileMetadata(doc, oldName, newName));
        if (metadata.getFolderName().equals(oldName))
            metadata.setFolderName(newName);

    }


    public FileMetaDataDto updateFileName(String id, String newFileName) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND_EXCEPTION, "File with id: " + id + " not found"));

        String newDirectoryName = fileMetadata.getFilePath() + "/" + newFileName;

        Path oldFilePath = Paths.get(fileMetadata.getFilePath(), fileMetadata.getFileName());
        Path newFilePath = Paths.get(fileMetadata.getFilePath(), newFileName);
        if (!Files.exists(oldFilePath)) {
            throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND_EXCEPTION, "File not found " + oldFilePath);

        }
        try {
            Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FailedToUpdateResourcesException(ErrorCode.FAILED_TO_UPDATE_FILE_EXCEPTION,
                    "Failed to update file name from " + fileMetadata.getFileName() + " to " + newFileName);
        }

        folderMetadatarepository.findByFolderPath(fileMetadata.getFilePath())
                .ifPresent(folderMetadata -> {
                    folderMetadata.getDocuments().stream()
                            .filter(document -> document.getFileName().equals(fileMetadata.getFileName()) &&
                                    document.getFilePath().equals(fileMetadata.getFilePath()))
                            .findFirst()
                            .ifPresent(document -> {
                                document.setFileName(newFileName);
                                document.setDirectoryName(newDirectoryName);
                            });
                    folderMetadatarepository.save(folderMetadata);
                });
        fileMetadata.setFileName(newFileName);
        fileMetadata.setDirectoryName(newDirectoryName);
        return convertToDto(fileMetadataRepository.save(fileMetadata), FileMetaDataDto.class);
    }

    public FolderMetadataDto generateDirectory(FolderRequestDto folderRequestDto) {
        return checkIfPathAlreadyExists(folderRequestDto.getFolderPath(), folderRequestDto.getFolderName());
    }

    private FolderMetadataDto checkIfPathAlreadyExists(String path, String folderName) {
        Optional<FolderMetadata> folderMetadata = folderMetadatarepository.findByFolderPath(path);
        if (folderMetadata.isPresent()) {
            throw new FolderAlreadyExistsException(ErrorCode.FOLDER_ALREADY_EXISTS_EXCEPTION, "Folder already exists cannot create folder on same path");
        }
        createDirectory(path);
        FolderMetadata folderMetadata1 = new FolderMetadata();
        folderMetadata1.setFolderPath(path);
        folderMetadata1.setWorkSpaceType(Constants.WORKSPACE_TYPE.MANUALWORKSPACE);
        folderMetadata1.setFolderName(folderName);
        return this.convertToDto(folderMetadatarepository.save(folderMetadata1), FolderMetadataDto.class);

    }
}
