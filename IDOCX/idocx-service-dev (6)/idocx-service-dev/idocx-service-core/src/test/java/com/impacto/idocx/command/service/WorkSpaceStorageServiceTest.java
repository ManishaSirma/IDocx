package com.impacto.idocx.command.service;

import com.impacto.idocx.command.common.Constants;
import com.impacto.idocx.command.dao.FileMetadataRepository;
import com.impacto.idocx.command.dao.FolderMetadataRepository;
import com.impacto.idocx.command.dtos.FileMetaDataDto;
import com.impacto.idocx.command.dtos.FolderMetadataDto;
import com.impacto.idocx.command.dtos.FolderRequestDto;
import com.impacto.idocx.command.entity.FileMetadata;
import com.impacto.idocx.command.entity.FolderMetadata;
import com.impacto.idocx.command.exceptions.ErrorCode;
import com.impacto.idocx.command.exceptions.FileNotFoundException;
import com.impacto.idocx.command.model.FileUploadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WorkSpaceStorageServiceTest {
    public static final String AUTOWORKSPACE = "AUTOWORKSPACE";
    public static final String ID = "1";
    public static final String FOLDER_NAME = "FolderName";
    public static final String FILENAME = "FileName";
    public static final String FILENAMEWITHEXTENSION = "FileName.txt";
    public static final String FILE = "file";
    public static final String FILE_CONTENT = "File Content..!";
    public static final String DOC = "DOC";
    private static final String DIRECTORY_PATH = "abc/Workpace/Sample";
    public static final String TEST_DIR_नमस्ते_こんにちは = "testDir_नमस्ते_こんにちは";
    public static final String TAG = "Tag";
    public static final String REAMRKS = "Reamrks";
    public static final String THERE_IS_NO_FILES_IN_THE_REQUEST = "There is no files in the request";
    private WorkSpaceStorageService workSpaceStorageService;
    private FileMetadataRepository fileMetadataRepository;
    private Environment environmentMock;
    private FolderMetadataRepository folderMetadatarepository;
    private ModelMapper modelMapper;
    private MultipartFile file1;
    private MultipartFile file2;
    private List<MultipartFile> files;
    private FileUploadRequest fileUploadRequest;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        environmentMock = mock(Environment.class);
        fileMetadataRepository = mock(FileMetadataRepository.class);
        when(environmentMock.getProperty("file.storage.location")).thenReturn(tempDir.toString());
        folderMetadatarepository = mock(FolderMetadataRepository.class);
        modelMapper = mock(ModelMapper.class);
        workSpaceStorageService = new WorkSpaceStorageService(environmentMock, fileMetadataRepository, folderMetadatarepository, modelMapper);
        fileUploadRequest = mock(FileUploadRequest.class);
        file1 = new MockMultipartFile(FILE, FILENAME, MediaType.TEXT_PLAIN_VALUE, FILE_CONTENT.getBytes());
        file2 = new MockMultipartFile(FILE, FILENAME, MediaType.TEXT_PLAIN_VALUE, FILE_CONTENT.getBytes());
        files = Arrays.asList(file1, file2);
    }

    @Test
    void testCreateDirectory() {
        String directoryName = "testDir";
        workSpaceStorageService.createDirectory(directoryName);
        assertTrue(Files.exists(tempDir.resolve(directoryName)));
    }

    @Test
    void testCreateDirectoryAlreadyExists() {
        String existingDirectoryName = "existingDir";
        Path existingDir = tempDir.resolve(existingDirectoryName);
        assertDoesNotThrow(() -> Files.createDirectories(existingDir));
        workSpaceStorageService.createDirectory(existingDirectoryName);
        assertTrue(Files.exists(existingDir));
    }

    @Test
    void testCreateDirectoryWithUnicodeCharacters() {
        String directoryName = TEST_DIR_नमस्ते_こんにちは;
        workSpaceStorageService.createDirectory(directoryName);
        assertTrue(Files.exists(tempDir.resolve(directoryName)));
    }

    @Test
    void testCreateDirectoryWithMaximumLengthName() {
        StringBuilder longDirectoryName = new StringBuilder();
        for (int i = 0; i < 255; i++) {
            longDirectoryName.append("a");
        }
        workSpaceStorageService.createDirectory(longDirectoryName.toString());
        assertTrue(Files.exists(tempDir.resolve(longDirectoryName.toString())));
    }

    @Test
    void testCreateMultipleDirectories() {
        String[] directoryNames = {"dir1", "dir2", "dir3"};
        for (String directoryName : directoryNames) {
            workSpaceStorageService.createDirectory(directoryName);
        }
        for (String directoryName : directoryNames) {
            assertTrue(Files.exists(tempDir.resolve(directoryName)));
        }
    }

    @Test
    void testStore() {
        workSpaceStorageService.store(createFileUploadRequestTest());
        verify(fileMetadataRepository, times(2)).save(any());
    }

    @Test
    void testStoreWithEmptyFile() {
        FileUploadRequest fileUploadRequest = createFileUploadRequestTest();
        fileUploadRequest.setFiles(Arrays.asList(new MockMultipartFile(FILE, FILENAME, MediaType.TEXT_PLAIN_VALUE, "".getBytes())));

        FileNotFoundException exception = assertThrows(FileNotFoundException.class,
                () -> workSpaceStorageService.store(fileUploadRequest));

        assertEquals(ErrorCode.FILE_NOT_FOUND_EXCEPTION, exception.getErrorCode());
        assertEquals(THERE_IS_NO_FILES_IN_THE_REQUEST, exception.getMessage());
    }

    @Test
    void testProcessFileMetadata() {
        String directoryName = "testDirectory";
        String fileName = "testFile.txt";

        FileMetadata existingMetadata = new FileMetadata();
        existingMetadata.setDirectoryName("differentDirectory");
        when(fileMetadataRepository.findByFileNameAndFilePath(eq(fileName), anyString()))
                .thenReturn(Optional.of(existingMetadata));
        when(fileUploadRequest.getWorkspaceType()).thenReturn(AUTOWORKSPACE);

        FolderMetadata folderMetadata = new FolderMetadata();
        folderMetadata.setDocuments(new ArrayList<>());
        when(folderMetadatarepository.findByFolderPath(anyString())).thenReturn(Optional.of(folderMetadata));

        MockMultipartFile mockFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Test content".getBytes());
        when(fileUploadRequest.getFiles()).thenReturn(Collections.singletonList(mockFile));
        when(fileUploadRequest.getFilePath()).thenReturn("testFilePath");

        workSpaceStorageService.store(fileUploadRequest);

        verify(fileMetadataRepository).findByFileNameAndFilePath(eq(fileName), anyString());
        verify(fileMetadataRepository).save(existingMetadata);

        verify(folderMetadatarepository).findByFolderPath(anyString());
    }

    @Test
    void testCreateExtension() {
        String extension = workSpaceStorageService.createExtension(FILENAMEWITHEXTENSION);
        assertEquals(".txt", extension);
        extension = workSpaceStorageService.createExtension(DOC);
        assertEquals("", extension);
    }

    @Test
    void testCreateExtensionWithFileWithMultipleLeadingDots() {
        String extension = workSpaceStorageService.createExtension("...file.docx");
        assertEquals(".docx", extension);
    }

    @Test
    void testLoadFileAsResource() throws IOException {
        String documentId = "testDocumentId";
        String directoryName = createTemporaryDirectory().getAbsolutePath();

        FileMetadata fileMetadata = new FileMetadata("testFileName", "testFilePath", directoryName);
        when(fileMetadataRepository.findById(documentId)).thenReturn(Optional.of(fileMetadata));

        Resource resource = workSpaceStorageService.loadFileAsResource(documentId);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void testGetDestinationFile1() {
        Path expectedPath = tempDir.resolve(DIRECTORY_PATH).resolve(FILENAME);
        Path destinationFile = workSpaceStorageService.getDestinationFile(DIRECTORY_PATH, file1);
        assertEquals(expectedPath, destinationFile);
    }

    @Test
    void testGetDestinationFile2() throws IOException {
        String directoryName = "testDirectory";
        String fileName = "testFile.txt";
        MockMultipartFile file = new MockMultipartFile("file", fileName, "text/plain", "Hello, World!".getBytes());
        Path expectedPath = tempDir.resolve(directoryName).resolve(fileName);

        Path actualPath = workSpaceStorageService.getDestinationFile(directoryName, file);

        assertEquals(expectedPath, actualPath);
    }

    @Test
    void testGetAllFolderStructure() {
        List<FolderMetadata> folderMetadataList = Arrays.asList(
                createFolderMetadataTest(FOLDER_NAME + 1, List.of()),
                createFolderMetadataTest(FOLDER_NAME + 2, List.of())
        );

        when(folderMetadatarepository.findByWorkSpaceTypeAndIsTrashFalseAndIsArchiveFalse(AUTOWORKSPACE)).thenReturn(folderMetadataList);
        List<FolderMetadataDto> result = workSpaceStorageService.getAllFolderStructure(AUTOWORKSPACE);
        List<FolderMetadataDto> expected = folderMetadataList.stream()
                .map(folderMetadata -> modelMapper.map(folderMetadata, FolderMetadataDto.class))
                .collect(Collectors.toList());

        assertEquals(expected, result);
    }

    @Test
    void testUpdateFolderName() throws IOException {
        String newFolderName = "newName";
        FileMetadata fileMetadata1 = createFileMetadataTest(FILENAME + 1);
        FileMetadata fileMetadata2 = createFileMetadataTest(FILENAME + 2);
        List<FileMetadata> fileMetadataList = Arrays.asList(fileMetadata1, fileMetadata2);

        FolderMetadata folderMetadata1 = createFolderMetadataTest(FOLDER_NAME + 1, fileMetadataList);
        FolderMetadata folderMetadata2 = createFolderMetadataTest(FOLDER_NAME + 2, fileMetadataList);
        List<FolderMetadata> folderMetadataList = Arrays.asList(folderMetadata1, folderMetadata2);

        FolderMetadataDto folderMetadataDto1 = createFolderMetadataDtoTest(FOLDER_NAME);

        Path dir = tempDir.resolve(folderMetadata1.getFolderPath());
        Files.createDirectories(dir);


        when(folderMetadatarepository.findById(anyString())).thenReturn(Optional.of(folderMetadata1));
        when(fileMetadataRepository.findAllByWorkSpaceType(anyString())).thenReturn(fileMetadataList);
        when(fileMetadataRepository.saveAll(any())).thenReturn(fileMetadataList);
        when(folderMetadatarepository.findAllByWorkSpaceType(any())).thenReturn(folderMetadataList);
        when(folderMetadatarepository.saveAll(any())).thenReturn(folderMetadataList);
        when(modelMapper.map(any(), any())).thenReturn(folderMetadataDto1);

        FolderMetadataDto actualResult = workSpaceStorageService.updateFolderName(ID, newFolderName);

        assertEquals(folderMetadataDto1.getFolderName(), actualResult.getFolderName());
        assertEquals(folderMetadataDto1.getFolderPath(), actualResult.getFolderPath());
    }

    @Test
    void testGetFilesMetadata() {

        List<FileMetadata> fileMetadataList = new ArrayList<>();
        fileMetadataList.add(new FileMetadata());
        fileMetadataList.add(new FileMetadata());
        Page<FileMetadata> mockedPage = new PageImpl<>(fileMetadataList);
        when(fileMetadataRepository.findAllByFilePathAndIsTrashFalseAndIsArchiveFalse(eq("mockFilePath"), any(PageRequest.class))).thenReturn(mockedPage);

        List<FileMetaDataDto> expectedDtoList = new ArrayList<>();
        expectedDtoList.add(new FileMetaDataDto());
        expectedDtoList.add(new FileMetaDataDto());
        when(modelMapper.map(any(FileMetadata.class), eq(FileMetaDataDto.class)))
                .thenReturn(expectedDtoList.get(0), expectedDtoList.get(1));

        Page<FileMetaDataDto> actualDtoList = workSpaceStorageService.getFilesMetadata("mockFilePath", 0, 10);
        assertEquals(fileMetadataList.size(), actualDtoList.getContent().size());

    }

    @Test
    void testUpdateFileName() throws IOException {
        String id = "testId";
        String newFileName = "newTestFileName";
        String filePath = createTemporaryDirectory1().getAbsolutePath();
        String oldFileName = "oldTestFileName";
        Path oldFilePath = Paths.get(filePath, oldFileName);
        Files.createFile(oldFilePath);

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileName(oldFileName);
        fileMetadata.setFilePath(filePath);
        fileMetadata.setId(id);

        FileMetaDataDto fileMetadataDto = new FileMetaDataDto();
        fileMetadataDto.setId(id);
        fileMetadataDto.setFileName(newFileName);
        fileMetadataDto.setFilePath(filePath);

        when(fileMetadataRepository.findById(id)).thenReturn(Optional.of(fileMetadata));

        FolderMetadata folderMetadata = new FolderMetadata();
        List<FileMetadata> documents = new ArrayList<>();
        documents.add(fileMetadata);
        folderMetadata.setDocuments(documents);
        folderMetadata.setId("mockFolderId");
        folderMetadata.setFolderPath("/path/to/file");
        when(folderMetadatarepository.findByFolderPath(filePath)).thenReturn(Optional.of(folderMetadata));
        when(folderMetadatarepository.save(any(FolderMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileMetadataRepository.save(fileMetadata)).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(), any())).thenReturn(fileMetadataDto);

        FileMetaDataDto result = workSpaceStorageService.updateFileName(id, newFileName);

        assertNotNull(result);
        assertEquals(newFileName, result.getFileName());
        assertTrue(Files.exists(Paths.get(filePath, newFileName)));
    }

    @Test
    void testGenerateDirectory_EmptyPath() {
        FolderRequestDto folderRequestDto = mock(FolderRequestDto.class);
        when(folderRequestDto.getFolderPath()).thenReturn("");
        when(folderRequestDto.getFolderName()).thenReturn("testFolder");

        FolderMetadataDto result = workSpaceStorageService.generateDirectory(folderRequestDto);

        assertNull(result);
    }

    @Test
    void testGenerateDirectory_EmptyName() {
        FolderRequestDto folderRequestDto = mock(FolderRequestDto.class);
        when(folderRequestDto.getFolderPath()).thenReturn("test/path");
        when(folderRequestDto.getFolderName()).thenReturn("");

        FolderMetadataDto result = workSpaceStorageService.generateDirectory(folderRequestDto);

        assertNull(result);
    }

    @Test
    void testGenerateDirectory_NullName() {
        FolderRequestDto folderRequestDto = mock(FolderRequestDto.class);
        when(folderRequestDto.getFolderPath()).thenReturn("test/path");
        when(folderRequestDto.getFolderName()).thenReturn(null);

        FolderMetadataDto result = workSpaceStorageService.generateDirectory(folderRequestDto);

        assertNull(result);
    }

    @Test
    void testGenerateDirectory_FolderAlreadyExists_ExceptionThrown() {
        FolderRequestDto folderRequestDto = new FolderRequestDto();
        folderRequestDto.setFolderPath("/existing/path");
        folderRequestDto.setFolderName("ExistingFolder");

        when(folderMetadatarepository.findByFolderPath(anyString()))
                .thenReturn(Optional.of(new FolderMetadata()));

        assertThrows(RuntimeException.class, () -> workSpaceStorageService.generateDirectory(folderRequestDto));
    }

    private File createTemporaryDirectory1() {
        try {
            return Files.createTempDirectory("temp").toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary directory", e);
        }
    }


    private File createTemporaryDirectory() {
        try {
            return File.createTempFile("temp", Long.toString(System.nanoTime()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary directory", e);
        }
    }


    private FileMetadata createFileMetadataTest(String fileName) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(ID);
        fileMetadata.setFileName(fileName);
        fileMetadata.setFilePath(DIRECTORY_PATH + "/" + fileName);
        fileMetadata.setDirectoryName(DIRECTORY_PATH);
        fileMetadata.setWorkSpaceType(Constants.WORKSPACE_TYPE.AUTOWORKSPACE);
        return fileMetadata;
    }

    private FileMetaDataDto createFileMetadataDtoTest(String fileName) {
        FileMetaDataDto fileMetaDataDto = new FileMetaDataDto();
        fileMetaDataDto.setId(ID);
        fileMetaDataDto.setFileName(fileName);
        fileMetaDataDto.setFilePath(DIRECTORY_PATH + "/" + fileName);
        fileMetaDataDto.setDirectoryName(DIRECTORY_PATH);
        fileMetaDataDto.setWorkSpaceType(Constants.WORKSPACE_TYPE.AUTOWORKSPACE);
        return fileMetaDataDto;
    }

    private FolderMetadata createFolderMetadataTest(String FolderName, List<FileMetadata> fileMetadataList) {
        FolderMetadata folderMetadata = new FolderMetadata();
        folderMetadata.setId(ID);
        folderMetadata.setFolderName(FolderName);
        folderMetadata.setFolderPath(DIRECTORY_PATH + "/" + FolderName);
        folderMetadata.setWorkSpaceType(Constants.WORKSPACE_TYPE.AUTOWORKSPACE);
        folderMetadata.setDocuments(fileMetadataList);
        return folderMetadata;
    }

    private FolderMetadataDto createFolderMetadataDtoTest(String FolderName) {
        FolderMetadataDto folderMetadataDto = new FolderMetadataDto();
        folderMetadataDto.setId(ID);
        folderMetadataDto.setFolderName(FolderName);
        folderMetadataDto.setFolderPath(DIRECTORY_PATH + "/" + FolderName);
        folderMetadataDto.setWorkSpaceType(Constants.WORKSPACE_TYPE.AUTOWORKSPACE);
        return folderMetadataDto;
    }

    private FileUploadRequest createFileUploadRequestTest() {
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setFilePath(DIRECTORY_PATH);
        fileUploadRequest.setFiles(files);
        fileUploadRequest.setPasswordProtected(false);
        fileUploadRequest.setTag(TAG);
        fileUploadRequest.setVersion(1);
        fileUploadRequest.setRemarks(REAMRKS);
        fileUploadRequest.setWorkspaceType(AUTOWORKSPACE);
        return fileUploadRequest;
    }

}

