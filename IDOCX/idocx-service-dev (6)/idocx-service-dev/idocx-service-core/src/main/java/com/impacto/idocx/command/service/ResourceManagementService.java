package com.impacto.idocx.command.service;

import com.impacto.idocx.command.common.Constants;
import com.impacto.idocx.command.common.GenericResponse;
import com.impacto.idocx.command.dao.FileMetadataRepository;
import com.impacto.idocx.command.dao.FolderMetadataRepository;
import com.impacto.idocx.command.dtos.FileMetaDataDto;
import com.impacto.idocx.command.dtos.FolderMetadataDto;
import com.impacto.idocx.command.dtos.IdsRequestDto;
import com.impacto.idocx.command.entity.FileMetadata;
import com.impacto.idocx.command.entity.FolderMetadata;
import com.impacto.idocx.command.exceptions.ErrorCode;
import com.impacto.idocx.command.exceptions.FailedToDeleteResorceException;
import com.impacto.idocx.command.exceptions.FileNotFoundException;
import com.impacto.idocx.command.exceptions.ResourceNotFoundException;
import com.impacto.idocx.command.exceptions.UnsupportedException;
import com.impacto.idocx.command.model.ResourceManagementRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.impacto.idocx.command.common.Constants.RESOURCES_ACTION.ARCHIVE;
import static com.impacto.idocx.command.common.Constants.RESOURCES_ACTION.FAVOURITE;


@Service
@RequiredArgsConstructor
@Log4j2
public class ResourceManagementService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FolderMetadataRepository folderMetadataRepository;
    private final ModelMapper modelMapper;

    public GenericResponse<?> updateResourceStatus(ResourceManagementRequest uploadRequest) {
        if (!Arrays.stream(Constants.RESOURCE_TYPE.values())
                .anyMatch(enumValue -> enumValue.name().equals(uploadRequest.getResourceType().toUpperCase())))
            throw new UnsupportedException(ErrorCode.UNSUPPORTED_EXCEPTION, "Unsupported resource type: " + uploadRequest.getResourceType());
        Constants.RESOURCE_TYPE resourcestype = Constants.RESOURCE_TYPE.valueOf(uploadRequest.getResourceType().toUpperCase());
        return switch (resourcestype) {
            case DOCUMENT -> updateDocumentsStatus(uploadRequest);
            case FOLDER -> updateFoldersStatus(uploadRequest);
        };
    }

    public GenericResponse<List<FileMetaDataDto>> updateDocumentsStatus(ResourceManagementRequest uploadRequest) {
        if (!Arrays.stream(Constants.RESOURCES_ACTION.values())
                .anyMatch(enumValue -> enumValue.name().equals(uploadRequest.getAction().toUpperCase())))
            throw new UnsupportedException(ErrorCode.UNSUPPORTED_EXCEPTION, "Unsupported resource action: " + uploadRequest.getResourceType());
        Constants.RESOURCES_ACTION resourcesActionAction = Constants.RESOURCES_ACTION.valueOf(uploadRequest.getAction().toUpperCase());
        boolean status = uploadRequest.isStatus();

        List<FileMetaDataDto> fileMetaDataDtos = new ArrayList<>();

        for (String documentId : uploadRequest.getIds()) {
            FileMetadata fileMetadata = fileMetadataRepository.findById(documentId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND_EXCEPTION, "File with id: " + documentId + " not found"));
            switch (resourcesActionAction) {
                case FAVOURITE:
                    fileMetadata.setFavourite(status);
                    break;
                case ARCHIVE:
                    fileMetadata.setArchive(status);
                    break;
                case TRASH:
                    fileMetadata.setTrash(status);
                    break;
            }
            FileMetadata savedMetadata = fileMetadataRepository.save(fileMetadata);
            FolderMetadata folderMetadata = folderMetadataRepository.findByFolderPath(savedMetadata.getFilePath())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND_EXCEPTION, "Folder with this filePath: " + savedMetadata.getFilePath() + " not found"));
            folderMetadata.getDocuments().forEach(document -> {
                if (document.getFileName().equals(savedMetadata.getFileName())) {
                    document.setFavourite(savedMetadata.isFavourite());
                    document.setArchive(savedMetadata.isArchive());
                    document.setTrash(savedMetadata.isTrash());
                }
            });
            folderMetadataRepository.save(folderMetadata);
            fileMetaDataDtos.add(convertToDto(savedMetadata, FileMetaDataDto.class));
        }
        return new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                fileMetaDataDtos
        );
    }

    private GenericResponse<List<FolderMetadataDto>> updateFoldersStatus(ResourceManagementRequest uploadRequest) {
        if (!Arrays.stream(Constants.RESOURCES_ACTION.values())
                .anyMatch(enumValue -> enumValue.name().equals(uploadRequest.getAction().toUpperCase())))
            throw new UnsupportedException(ErrorCode.UNSUPPORTED_EXCEPTION, "Unsupported resource action: " + uploadRequest.getResourceType());

        Constants.RESOURCES_ACTION resourcesActionAction = Constants.RESOURCES_ACTION.valueOf(uploadRequest.getAction().toUpperCase());
        boolean status = uploadRequest.isStatus();
        List<FolderMetadataDto> folderMetadataDtos = new ArrayList<>();

        for (String folderId : uploadRequest.getIds()) {
            FolderMetadata folderMetadata = folderMetadataRepository.findById(folderId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND_EXCEPTION, "Folder with id: " + folderId + " not found"));
            switch (resourcesActionAction) {
                case FAVOURITE:
                    folderMetadata.setFavourite(status);
                    break;
                case ARCHIVE:
                    folderMetadata.setArchive(status);
                    break;
                case TRASH:
                    folderMetadata.setTrash(status);
                    break;
            }
            FolderMetadata savedMetadata = folderMetadataRepository.save(folderMetadata);
            folderMetadataDtos.add(convertToDto(savedMetadata, FolderMetadataDto.class));
        }
        return new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                folderMetadataDtos
        );
    }

    public Page<?> getResources(String resourcesAction, String resourcesType, int pageNo, int pageSize) {
        if (!(Arrays.stream(Constants.RESOURCES_ACTION.values())
                .anyMatch(enumValue -> enumValue.name().equals(resourcesAction.toUpperCase())) ||
                Arrays.stream(Constants.RESOURCE_TYPE.values())
                        .anyMatch(enumValue -> enumValue.name().equals(resourcesType.toUpperCase())))) {
            throw new UnsupportedException(ErrorCode.UNSUPPORTED_EXCEPTION,
                    "Unsupported resource action or type: " + resourcesAction + ", " + resourcesType);
        }
        Constants.RESOURCE_TYPE type = Constants.RESOURCE_TYPE.valueOf(resourcesType.toUpperCase());
        Constants.RESOURCES_ACTION action = Constants.RESOURCES_ACTION.valueOf(resourcesAction.toUpperCase());
        switch (type) {
            case DOCUMENT -> {
                if (action.equals(FAVOURITE))
                    return fileMetadataRepository.findByIsFavouriteTrue(PageRequest.of(pageNo, pageSize))
                            .map(entity -> convertToDto(entity, FileMetaDataDto.class));
                else if (action.equals(ARCHIVE))
                    return fileMetadataRepository.findByIsArchiveTrue(PageRequest.of(pageNo, pageSize))
                            .map(entity -> convertToDto(entity, FileMetaDataDto.class));
                else
                    return fileMetadataRepository.findByIsTrashTrue(PageRequest.of(pageNo, pageSize))
                            .map(entity -> convertToDto(entity, FileMetaDataDto.class));

            }
            default -> {
                if (action.equals(FAVOURITE))
                    return folderMetadataRepository.findByIsFavouriteTrue(PageRequest.of(pageNo, pageSize))
                            .map(entity -> convertToDto(entity, FolderMetadataDto.class));
                else if (action.equals(ARCHIVE))
                    return folderMetadataRepository.findByIsArchiveTrue(PageRequest.of(pageNo, pageSize))
                            .map(entity -> convertToDto(entity, FolderMetadataDto.class));
                else
                    return folderMetadataRepository.findByIsTrashTrue(PageRequest.of(pageNo, pageSize))
                            .map(entity -> convertToDto(entity, FolderMetadataDto.class));
            }
        }
    }

    public <S, T> T convertToDto(S source, Class<T> targetClass) {
        return modelMapper.map(source, targetClass);
    }

    public void deleteResource(IdsRequestDto idsRequestDto) {
        checkForFileOrFolderDeletion(idsRequestDto);
    }

    private void checkForFileOrFolderDeletion(IdsRequestDto idsRequestDto) {
        if (Constants.RESOURCE_TYPE.FOLDER.name().equalsIgnoreCase(idsRequestDto.getType())) {
            idsRequestDto.getIds().forEach(this::trashFolder);
        } else {
            idsRequestDto.getIds().forEach(this::trashFile);
        }
    }

    void trashFolder(String id) {
        log.info("Processing folder with ID: {}", id);
        FolderMetadata folderMetadata = folderMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND_EXCEPTION, "Folder with id: " + id + " not found"));
        deleteFileOrFolder(folderMetadata.getFolderPath(), true);
        folderMetadataRepository.delete(folderMetadata);
        deleteRelatedDocuments(folderMetadata);
    }

    void trashFile(String id) {
        log.info("Processing file with ID: {}", id);
        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND_EXCEPTION, "File with id: " + id + " not found"));
        deleteFileOrFolder(fileMetadata.getDirectoryName(), false);
        fileMetadataRepository.delete(fileMetadata);
        FolderMetadata folderMetadata = folderMetadataRepository.findByFolderPath(fileMetadata.getFilePath()).orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND_EXCEPTION, "folder not found"));
        folderMetadata.getDocuments().removeIf(document -> document.getDirectoryName().equals(fileMetadata.getDirectoryName()));
        folderMetadataRepository.save(folderMetadata);

    }

    private void deleteRelatedDocuments(FolderMetadata folderMetadata) {
        log.info("Deleting related documents in folder: {}", folderMetadata.getFolderPath());
        folderMetadata.getDocuments().forEach(file -> {
            FileMetadata fileMetadata = fileMetadataRepository.findByFileName(file.getFileName()).orElseThrow(() -> new FileNotFoundException(ErrorCode.FILE_NOT_FOUND_EXCEPTION, "File not found for file name : " + file.getFileName()));
            fileMetadataRepository.delete(fileMetadata);
        });
    }

    public void deleteFileOrFolder(String pathStr, boolean isFolder) {
        log.info("Deleting {} at path: {}", isFolder ? "folder" : "file", pathStr);
        Path path = Paths.get(pathStr);
        File file = path.toFile();

        if (!file.exists()) {
            throw new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND_EXCEPTION, "File or folder does not exist: " + pathStr);
        }

        try {
            iteratingAndDeletingResources(pathStr, isFolder, file, path);
        } catch (IOException e) {
            throw new FailedToDeleteResorceException(ErrorCode.FAILED_TO_DELETE_RESOURCE_EXCEPTION, "Error during deletion: " + e.getMessage());
        }
    }

    private static void iteratingAndDeletingResources(String pathStr, boolean isFolder, File file, Path path) throws IOException {
        if (file.isDirectory() && isFolder) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(f -> {
                        if (!f.delete()) {
                            throw new FailedToDeleteResorceException(ErrorCode.FAILED_TO_DELETE_RESOURCE_EXCEPTION, "Failed to delete " + f.getAbsolutePath()); // Consider using a specific exception
                        }
                    });
        } else if (!file.isDirectory() || !isFolder) {
            if (!file.delete()) {
                throw new FailedToDeleteResorceException(ErrorCode.FAILED_TO_DELETE_RESOURCE_EXCEPTION, "Failed to delete the file: " + pathStr);
            }
        }
        log.info("Successfully deleted {} the path: {}", isFolder ? "folder" : "file", pathStr);
    }

}
