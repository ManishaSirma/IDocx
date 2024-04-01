package com.impacto.idocx.command.api;

import com.impacto.idocx.command.common.Constants;
import com.impacto.idocx.command.common.GenericResponse;
import com.impacto.idocx.command.dtos.FileMetaDataDto;
import com.impacto.idocx.command.dtos.FolderMetadataDto;
import com.impacto.idocx.command.dtos.FolderRequestDto;
import com.impacto.idocx.command.model.FileUploadRequest;
import com.impacto.idocx.command.service.WorkSpaceStorageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workspace")
@Log4j2
public class WorkSpaceController {

    private final ServletContext servletContext;
    private final WorkSpaceStorageService workSpaceStorageService;

    @Operation(summary= "Upload File", description= "Uploads one or more files to the server.")
    @PostMapping("/upload")
    public ResponseEntity<GenericResponse<String>> uploadFile(@ModelAttribute FileUploadRequest uploadRequest) {
        workSpaceStorageService.store(uploadRequest);
        GenericResponse<String> response = new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                "File uploaded successfully to : " + uploadRequest.getFilePath()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @Operation(summary = "Download File by ID",
            description = "This endpoint allows users to download a file by specifying its ID. ")
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) throws IOException {
        try {
            Resource resource = workSpaceStorageService.loadFileAsResource(id);

            if (resource == null) {
                return ResponseEntity.notFound().build(); // Resource not found, return 404 status
            }

            String contentType = servletContext.getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace(); // Replace with a logger in production code
            return ResponseEntity.internalServerError().body(null);
        }

    }

    @Operation(summary = "Fetch Workspace Folder Structure", description = "Retrieve the folder structure of specific workspace.")
    @GetMapping("/{workspacename}")
    public ResponseEntity<GenericResponse<List<FolderMetadataDto>>> fetchWorkSpaceFolderStructure(@PathVariable String workspacename) {
        GenericResponse<List<FolderMetadataDto>> response = new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                workSpaceStorageService.getAllFolderStructure(workspacename)
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @Operation(summary = "Fetch File Metadata", description = "Retrieve metadata of files located at the specified file path.")
    @GetMapping("/documents")
    public ResponseEntity<GenericResponse<List<FileMetaDataDto>>> fetchFileMetadata(@RequestParam String filePath,
                                                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                                                    @RequestParam(defaultValue = "0") int pageNo
    ) {
        Page<FileMetaDataDto> fileMetaDataDtos = workSpaceStorageService.getFilesMetadata(filePath, pageNo, pageSize);
        GenericResponse<List<FileMetaDataDto>> response = new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                fileMetaDataDtos.getContent(),
                fileMetaDataDtos.getTotalElements(),
                fileMetaDataDtos.getNumberOfElements(),
                fileMetaDataDtos.getNumber()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @Operation(summary = "Update File Name", description = "Update the name of a file stored in the workspace.")
    @PutMapping("/update-file/{id}")
    public ResponseEntity<GenericResponse<FileMetaDataDto>> updateFileName(@PathVariable String id, @RequestParam String newFileName) {
        GenericResponse<FileMetaDataDto> response = new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                workSpaceStorageService.updateFileName(id, newFileName)
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @Operation(summary = "Update Folder Name", description = "Update the name of a folder with the specified ID.")
    @PutMapping("/update-folder/{id}")
    public ResponseEntity<GenericResponse<FolderMetadataDto>> updateFolderName(@PathVariable String id, @RequestParam String newFolderName) {
        GenericResponse<FolderMetadataDto> response = new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                workSpaceStorageService.updateFolderName(id, newFolderName)
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @Operation(summary = "Create Directory",
            description = "Creates a new directory in the workspace.")
    @PostMapping("/create-directory")
    public ResponseEntity<GenericResponse<FolderMetadataDto>> createDirectory(@RequestBody FolderRequestDto folderRequestDto) {
        GenericResponse<FolderMetadataDto> response = new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                workSpaceStorageService.generateDirectory(folderRequestDto)
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}