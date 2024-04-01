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
import com.impacto.idocx.command.exceptions.ResourceNotFoundException;
import com.impacto.idocx.command.exceptions.UnsupportedException;
import com.impacto.idocx.command.model.ResourceManagementRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceManagementServiceTest {
    public static final String DOCUMENT = "document";
    public static final String FAVOURITE = "favourite";
    private static final String DIRECTORY_PATH = "A";
    public static final String FOLDER = "folder";
    public static final String SUCCESS = "success";
    public static final String ARCHIVE = "archive";
    public static final String ID_1 = "id1";
    public static final String ID_2 = "id2";
    public static final String TRASH = "trash";
    public static final String FILE_TXT = "test.txt";
    public static final String TEST_FILE_SHOULD_EXIST_BEFORE_DELETION_ATTEMPT = "Test file should exist before deletion attempt.";
    public static final String TEST_FILE_SHOULD_BE_DELETED_AFTER_METHOD_CALL = "Test file should be deleted after method call.";
    public static final boolean TRUE = true;
    public static final int STATUS100 = 100;
    public static final String UNSUPPORTED_ACTION = "UNSUPPORTED_ACTION";
    @Mock
    private FileMetadataRepository fileMetadataRepository;
    @Mock
    private FolderMetadataRepository folderMetadataRepository;
    @Mock
    private ModelMapper mockModelMapper;
    @InjectMocks
    private ResourceManagementService resourceManagementService;
    private List<String> ids;
    @TempDir
    Path tempDir;

    @Test
    void testUpdateResourceStatus() {
        ids = Arrays.asList(ID_1, ID_2);
        ResourceManagementRequest favourite1 = new ResourceManagementRequest(FAVOURITE, DOCUMENT, ids, TRUE);
        ResourceManagementRequest favourite2 = new ResourceManagementRequest(FAVOURITE, FOLDER, ids, TRUE);
        ResourceManagementRequest archive1 = new ResourceManagementRequest(ARCHIVE, DOCUMENT, ids, TRUE);
        ResourceManagementRequest archive2 = new ResourceManagementRequest(ARCHIVE, FOLDER, ids, TRUE);
        ResourceManagementRequest trash1 = new ResourceManagementRequest(TRASH, DOCUMENT, ids, TRUE);
        ResourceManagementRequest trash2 = new ResourceManagementRequest(TRASH, FOLDER, ids, TRUE);

        FileMetadata fileMetadata = new FileMetadata(DOCUMENT, DIRECTORY_PATH + DOCUMENT, DIRECTORY_PATH);
        FolderMetadata folderMetadata = createFolderMetadataTest(FOLDER, List.of(fileMetadata));
        FileMetaDataDto fileMetaDataDto = createFileMetaDataDtoTest(DOCUMENT);
        FolderMetadataDto folderMetadataDto = createFolderMetadataDtoTest(FOLDER);
        List<FileMetaDataDto> fileMetaDataDtos = Arrays.asList(fileMetaDataDto, fileMetaDataDto);
        List<FolderMetadataDto> folderMetadataDtos = Arrays.asList(folderMetadataDto, folderMetadataDto);

        GenericResponse<List<FileMetaDataDto>> expectedFileDtoResponse = new GenericResponse<>(STATUS100, SUCCESS, fileMetaDataDtos);
        GenericResponse<List<FolderMetadataDto>> expectedFolderDtoResponse = new GenericResponse<>(STATUS100, SUCCESS, folderMetadataDtos);

        when(fileMetadataRepository.findById(anyString())).thenReturn(Optional.of(fileMetadata));
        when(fileMetadataRepository.save(any())).thenReturn(fileMetadata);
        when(folderMetadataRepository.findByFolderPath(anyString())).thenReturn(Optional.of(folderMetadata));
        when(folderMetadataRepository.findById(anyString())).thenReturn(Optional.of(folderMetadata));
        when(folderMetadataRepository.save(any())).thenReturn(folderMetadata);
        when(resourceManagementService.convertToDto(fileMetadata, FileMetaDataDto.class)).thenReturn(fileMetaDataDto);
        when(resourceManagementService.convertToDto(folderMetadata, FolderMetadataDto.class)).thenReturn(folderMetadataDto);

        GenericResponse<?> actualResponse = resourceManagementService.updateResourceStatus(favourite1);
        assertThat(actualResponse.getData()).isEqualTo(expectedFileDtoResponse.getData());

        actualResponse = resourceManagementService.updateResourceStatus(favourite2);
        assertThat(actualResponse.getData()).isEqualTo(expectedFolderDtoResponse.getData());

        actualResponse = resourceManagementService.updateResourceStatus(archive1);
        assertThat(actualResponse.getData()).isEqualTo(expectedFileDtoResponse.getData());

        actualResponse = resourceManagementService.updateResourceStatus(archive2);
        assertThat(actualResponse.getData()).isEqualTo(expectedFolderDtoResponse.getData());

        actualResponse = resourceManagementService.updateResourceStatus(trash1);
        assertThat(actualResponse.getData()).isEqualTo(expectedFileDtoResponse.getData());

        actualResponse = resourceManagementService.updateResourceStatus(trash2);
        assertThat(actualResponse.getData()).isEqualTo(expectedFolderDtoResponse.getData());
    }

    @Test
    void testUnsupportedResourceTypeException() {
        ids = Arrays.asList(ID_1, ID_2);
        ResourceManagementRequest unsupportedRequest = new ResourceManagementRequest(UNSUPPORTED_ACTION, DOCUMENT, ids, TRUE);
        ResourceManagementRequest unsupportedRequest2 = new ResourceManagementRequest(FAVOURITE, UNSUPPORTED_ACTION, ids, TRUE);
        ResourceManagementRequest unsupportedRequest3 = new ResourceManagementRequest(UNSUPPORTED_ACTION, FOLDER, ids, TRUE);
        assertThrows(UnsupportedException.class, () ->
                resourceManagementService.updateResourceStatus(unsupportedRequest));
        assertThrows(UnsupportedException.class, () ->
                resourceManagementService.updateResourceStatus(unsupportedRequest2));
        assertThrows(UnsupportedException.class, () ->
                resourceManagementService.updateResourceStatus(unsupportedRequest3));
    }


    @Test
    void testGetResources() {
        List<FileMetadata> fileMetadataList = new ArrayList<>();
        List<FolderMetadata> folderMetadataList = new ArrayList<>();

        Page<FileMetadata> page = new PageImpl<>(fileMetadataList);
        Page<FolderMetadata> page1 = new PageImpl<>(folderMetadataList);

        int pageNo = 0;
        int pageSize = 10;

        when(fileMetadataRepository.findByIsFavouriteTrue(any())).thenReturn(page);
        when(fileMetadataRepository.findByIsArchiveTrue(any())).thenReturn(page);
        when(fileMetadataRepository.findByIsTrashTrue(any())).thenReturn(page);
        when(folderMetadataRepository.findByIsFavouriteTrue(any())).thenReturn(page1);
        when(folderMetadataRepository.findByIsArchiveTrue(any())).thenReturn(page1);
        when(folderMetadataRepository.findByIsTrashTrue(any())).thenReturn(page1);

        Page<?> documentPage = resourceManagementService.getResources(FAVOURITE, DOCUMENT, pageNo, pageSize);
        assertEquals(0, documentPage.getNumberOfElements());
        Page<?> documentPage1 = resourceManagementService.getResources(ARCHIVE, DOCUMENT, pageNo, pageSize);
        assertEquals(0, documentPage.getNumberOfElements());
        Page<?> documentPage2 = resourceManagementService.getResources(TRASH, DOCUMENT, pageNo, pageSize);
        assertEquals(0, documentPage.getNumberOfElements());
        Page<?> folderPage = resourceManagementService.getResources(FAVOURITE, FOLDER, pageNo, pageSize);
        assertEquals(0, folderPage.getNumberOfElements());
        Page<?> folderPage1 = resourceManagementService.getResources(ARCHIVE, FOLDER, pageNo, pageSize);
        assertEquals(0, folderPage.getNumberOfElements());
        Page<?> folderPage2 = resourceManagementService.getResources(TRASH, FOLDER, pageNo, pageSize);
        assertEquals(0, folderPage.getNumberOfElements());
    }

    @Test
    void testGetResources_InvalidResourceAction() {
        assertThrows(IllegalArgumentException.class, () -> {
            resourceManagementService.getResources(UNSUPPORTED_ACTION, DOCUMENT, 0, 10);
        });
    }

    @Test
    void testGetResources_InvalidResourceType() {
        assertThrows(IllegalArgumentException.class, () -> {
            resourceManagementService.getResources(FAVOURITE, UNSUPPORTED_ACTION, 0, 10);
        });
    }


    @Test
    void testTrashFileSuccess() throws IOException {
        Path testFile = tempDir.resolve(FILE_TXT);
        Files.createFile(testFile);

        assertTrue(Files.exists(testFile), TEST_FILE_SHOULD_EXIST_BEFORE_DELETION_ATTEMPT);

        resourceManagementService.deleteFileOrFolder(testFile.toString(), false);

        assertFalse(Files.exists(testFile), TEST_FILE_SHOULD_BE_DELETED_AFTER_METHOD_CALL);
    }

    @Test
    void testTrashFileNotFound() {
        when(fileMetadataRepository.findById(ID_1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            resourceManagementService.trashFile(ID_1);
        });

        verify(fileMetadataRepository, never()).delete(any());
        verify(folderMetadataRepository, never()).save(any());
    }

    @Test
    void testDeleteResource() throws IOException {
        IdsRequestDto idsRequestDto = new IdsRequestDto();
        idsRequestDto.setIds(Arrays.asList(ID_1));
        idsRequestDto.setType(DOCUMENT);

        Path expectedPath1 = tempDir.resolve(FILE_TXT);
        Files.createFile(expectedPath1);

        FileMetadata fileMetadata1 = createFileMetaDataTest(DOCUMENT);
        fileMetadata1.setDirectoryName(expectedPath1.toString());

        ArrayList<FileMetadata> metadataArrayList = new ArrayList<>();
        metadataArrayList.add(fileMetadata1);
        FolderMetadata folderMetadata = createFolderMetadataTest(FOLDER, metadataArrayList);

        when(fileMetadataRepository.findById(anyString())).thenReturn(Optional.of(fileMetadata1));
        when(folderMetadataRepository.findByFolderPath(anyString())).thenReturn(Optional.of(folderMetadata));
        when(folderMetadataRepository.save(any())).thenReturn(folderMetadata);
        doNothing().when(fileMetadataRepository).delete(any());
        resourceManagementService.deleteResource(idsRequestDto);
        verify(fileMetadataRepository, times(1)).findById(anyString());

        idsRequestDto.setType(FOLDER);
        Path expectedPath = tempDir.resolve(DIRECTORY_PATH);
        Files.createDirectories(expectedPath);
        Files.createFile(tempDir.resolve(expectedPath).resolve(FILE_TXT));
        folderMetadata.setFolderPath(expectedPath.toString());
        when(folderMetadataRepository.findById(anyString())).thenReturn(Optional.of(folderMetadata));
        doNothing().when(folderMetadataRepository).delete(any());
        resourceManagementService.deleteResource(idsRequestDto);

    }

    private FolderMetadata createFolderMetadataTest(String folderName, List<FileMetadata> fileMetaDataDtos) {
        FolderMetadata folderMetadata = new FolderMetadata();
        folderMetadata.setFolderName(folderName);
        folderMetadata.setFolderPath(DIRECTORY_PATH);
        folderMetadata.setWorkSpaceType(Constants.WORKSPACE_TYPE.AUTOWORKSPACE);
        folderMetadata.setDocuments(fileMetaDataDtos);
        return folderMetadata;
    }

    private FolderMetadataDto createFolderMetadataDtoTest(String folderName) {
        FolderMetadataDto folderMetadataDto = new FolderMetadataDto();
        folderMetadataDto.setFolderName(folderName);
        folderMetadataDto.setFolderPath(DIRECTORY_PATH);
        folderMetadataDto.setWorkSpaceType(Constants.WORKSPACE_TYPE.AUTOWORKSPACE);
        return folderMetadataDto;
    }

    private FileMetadata createFileMetaDataTest(String documentName) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileName(documentName);
        fileMetadata.setFilePath(DIRECTORY_PATH + "/" + documentName);
        fileMetadata.setDirectoryName(DIRECTORY_PATH);
        return fileMetadata;
    }

    private FileMetaDataDto createFileMetaDataDtoTest(String documentName) {
        FileMetaDataDto fileMetadataDto = new FileMetaDataDto();
        fileMetadataDto.setFileName(documentName);
        fileMetadataDto.setFilePath(DIRECTORY_PATH + "/" + documentName);
        fileMetadataDto.setDirectoryName(DIRECTORY_PATH);
        return fileMetadataDto;
    }
}
