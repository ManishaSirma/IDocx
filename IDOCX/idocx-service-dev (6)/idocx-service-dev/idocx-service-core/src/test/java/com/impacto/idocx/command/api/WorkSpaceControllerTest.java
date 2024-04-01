package com.impacto.idocx.command.api;

import com.impacto.idocx.command.common.Constants;
import com.impacto.idocx.command.common.GenericResponse;
import com.impacto.idocx.command.dtos.FileMetaDataDto;
import com.impacto.idocx.command.dtos.FolderMetadataDto;
import com.impacto.idocx.command.model.FileUploadRequest;
import com.impacto.idocx.command.service.WorkSpaceStorageService;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkSpaceControllerTest {
    public static final String DOCUMENT = "DOCUMENT";
    public static final String FOLDERNAME = "FOLDERNAME";
    public static final String FILE = "file";
    public static final String FILE_CONTENT = "File Content..!";
    public static final String AUTOWORKSPACE = "AUTOWORKSPACE";
    private static final String FILE_UPLOADED_SUCCESSFUL_MESSAGE = "File uploaded successfully to : ";
    private static final String DIRECTORY_PATH = "ABC/DEF/IJK";
    @Mock
    private WorkSpaceStorageService workSpaceStorageService;
    @InjectMocks
    private WorkSpaceController workSpaceController;
    @Mock
    private ServletContext servletContext;
    private List<MultipartFile> files;
    private MultipartFile file1;
    private MultipartFile file2;

    @BeforeEach
    void setup() {
        file1 = new MockMultipartFile(FILE, "test1.txt", MediaType.TEXT_PLAIN_VALUE, FILE_CONTENT.getBytes());
        file2 = new MockMultipartFile(FILE, "test2.txt", MediaType.TEXT_PLAIN_VALUE, FILE_CONTENT.getBytes());
        files = Arrays.asList(file1, file2);
    }

    @Test
    void TestUploadFile() {
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setFiles(files);
        fileUploadRequest.setFilePath(DIRECTORY_PATH);
        fileUploadRequest.setWorkspaceType(AUTOWORKSPACE);

        doNothing().when(workSpaceStorageService).store(fileUploadRequest);
        ResponseEntity<GenericResponse<String>> responseEntity = workSpaceController.uploadFile(fileUploadRequest);
        GenericResponse<String> responseBody = responseEntity.getBody();

        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), responseBody.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), responseBody.getMessage());
        assertEquals(FILE_UPLOADED_SUCCESSFUL_MESSAGE + DIRECTORY_PATH, responseBody.getData());
        verify(workSpaceStorageService, times(1)).store(any());
    }

    @Test
    void TestDownloadFile1() throws Exception {
        String path = DIRECTORY_PATH + "/" + file1.getOriginalFilename();
        Path filePath = Paths.get(path);
        Resource resource = new UrlResource(filePath.toUri());

        when(workSpaceStorageService.loadFileAsResource(anyString())).thenReturn(resource);
        when(servletContext.getMimeType(anyString())).thenReturn("application/pdf");
        ResponseEntity<Resource> responseEntity = workSpaceController.downloadFile(path);

        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());
        verify(workSpaceStorageService, times(1)).loadFileAsResource(anyString());
    }

    @Test
    void TestDownloadFile2() throws IOException {
        String path = DIRECTORY_PATH + "/" + file1.getOriginalFilename();

        when(workSpaceStorageService.loadFileAsResource(anyString())).thenReturn(null);
        ResponseEntity<Resource> responseEntity = workSpaceController.downloadFile(path);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(null, responseEntity.getBody());
    }

    @Test
    void TestFetchWorkSpaceFolderStructure() {
        List<FileMetaDataDto> fileMetaDataDtos = Arrays.asList(
                createFileMetaDataDtoTest(DOCUMENT + "1"),
                createFileMetaDataDtoTest(DOCUMENT + "2")
        );

        List<FolderMetadataDto> folderMetadataDtos = Arrays.asList(
                createFolderMetadataDtoTest(FOLDERNAME + "1", fileMetaDataDtos),
                createFolderMetadataDtoTest(FOLDERNAME + "2", fileMetaDataDtos)
        );

        when(workSpaceStorageService.getAllFolderStructure(AUTOWORKSPACE)).thenReturn(folderMetadataDtos);
        ResponseEntity<GenericResponse<List<FolderMetadataDto>>> responseEntity = workSpaceController.fetchWorkSpaceFolderStructure(AUTOWORKSPACE);
        GenericResponse<List<FolderMetadataDto>> responseBody = responseEntity.getBody();

        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), responseBody.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), responseBody.getMessage());
        assertEquals(folderMetadataDtos.size(), responseBody.getData().size());
        verify(workSpaceStorageService, times(1)).getAllFolderStructure(any());
    }

    @Test
    void testFetchFileMetadata() {
        Page<FileMetaDataDto> mockPage = new PageImpl<>(Collections.singletonList(new FileMetaDataDto()), PageRequest.of(0, 10), 1);
        when(workSpaceStorageService.getFilesMetadata(anyString(), anyInt(), anyInt())).thenReturn(mockPage);
        ResponseEntity<GenericResponse<List<FileMetaDataDto>>> responseEntity = workSpaceController.fetchFileMetadata("sampleFilePath", 10, 0);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        GenericResponse<List<FileMetaDataDto>> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), responseBody.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), responseBody.getMessage());
        assertEquals(1, responseBody.getTotal());
        assertEquals(1, responseBody.getData().size());
    }

    @Test
    void testUpdateFolderName() {
        String folderId = "sampleFolderId";
        String newFolderName = "New Folder Name";
        FolderMetadataDto updatedFolderMetadata = new FolderMetadataDto();

        when(workSpaceStorageService.updateFolderName(anyString(), anyString())).thenReturn(updatedFolderMetadata);

        ResponseEntity<GenericResponse<FolderMetadataDto>> responseEntity = workSpaceController.updateFolderName(folderId, newFolderName);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        GenericResponse<FolderMetadataDto> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), responseBody.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), responseBody.getMessage());
        assertEquals(updatedFolderMetadata, responseBody.getData());
    }


    @Test
    void TestUpdateFileName() {
        FileMetaDataDto fileMetaDataDto = createFileMetaDataDtoTest(DOCUMENT);

        when(workSpaceStorageService.updateFileName(anyString(), anyString())).thenReturn(fileMetaDataDto);
        ResponseEntity<GenericResponse<FileMetaDataDto>> responseEntity = workSpaceController.updateFileName(anyString(), anyString());
        GenericResponse<FileMetaDataDto> responseBody = responseEntity.getBody();

        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), responseBody.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), responseBody.getMessage());
        assertEquals(fileMetaDataDto.getFileName(), responseBody.getData().getFileName());
        assertEquals(fileMetaDataDto.getFilePath(), responseBody.getData().getFilePath());
        assertEquals(fileMetaDataDto.getDirectoryName(), responseBody.getData().getDirectoryName());
        assertEquals(fileMetaDataDto.getWorkSpaceType(), responseBody.getData().getWorkSpaceType());
        verify(workSpaceStorageService, times(1)).updateFileName(anyString(), anyString());

    }

    @Test
    void TestCreateDirectory() {
        FolderMetadataDto folderMetadataDto = new FolderMetadataDto();
        folderMetadataDto.setFolderName(FOLDERNAME);
        folderMetadataDto.setFolderPath(DIRECTORY_PATH + FOLDERNAME);
        folderMetadataDto.setWorkSpaceType(Constants.WORKSPACE_TYPE.AUTOWORKSPACE);

        when(workSpaceStorageService.generateDirectory(any())).thenReturn(folderMetadataDto);
        ResponseEntity<GenericResponse<FolderMetadataDto>> responseEntity = workSpaceController.createDirectory(any());
        GenericResponse<FolderMetadataDto> responseBody = responseEntity.getBody();

        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), responseBody.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), responseBody.getMessage());
        assertEquals(folderMetadataDto.getFolderName(), responseBody.getData().getFolderName());
        assertEquals(folderMetadataDto.getFolderPath(), responseBody.getData().getFolderPath());
        assertEquals(folderMetadataDto.getWorkSpaceType(), responseBody.getData().getWorkSpaceType());
        verify(workSpaceStorageService, times(1)).generateDirectory(any());

    }

    private FileMetaDataDto createFileMetaDataDtoTest(String documentName) {
        FileMetaDataDto fileMetaDataDto = new FileMetaDataDto();
        fileMetaDataDto.setFileName(documentName);
        fileMetaDataDto.setFilePath(DIRECTORY_PATH + "/" + documentName);
        fileMetaDataDto.setDirectoryName(DIRECTORY_PATH);
        fileMetaDataDto.setWorkSpaceType(Constants.WORKSPACE_TYPE.AUTOWORKSPACE);
        return fileMetaDataDto;
    }

    private FolderMetadataDto createFolderMetadataDtoTest(String folderName, List<FileMetaDataDto> fileMetaDataDtos) {
        FolderMetadataDto folderMetadataDto = new FolderMetadataDto();
        folderMetadataDto.setFolderName(folderName);
        folderMetadataDto.setFolderPath(DIRECTORY_PATH);
        folderMetadataDto.setWorkSpaceType(Constants.WORKSPACE_TYPE.AUTOWORKSPACE);
        return folderMetadataDto;
    }
}