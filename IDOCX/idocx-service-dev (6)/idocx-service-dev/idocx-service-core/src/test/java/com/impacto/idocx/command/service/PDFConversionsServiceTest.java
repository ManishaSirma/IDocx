package com.impacto.idocx.command.service;

import com.impacto.idocx.command.common.Constants;
import com.impacto.idocx.command.common.GenericResponse;
import com.impacto.idocx.command.dao.FileMetadataRepository;
import com.impacto.idocx.command.entity.FileMetadata;
import com.impacto.idocx.command.exceptions.FailedToCompressResourcesException;
import com.impacto.idocx.command.exceptions.ResourceNotFoundException;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.PotentialStubbingProblem;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PDFConversionsServiceTest {

    public static final String ID = "id";
    public static final String FILE_NAME = "fileName";
    public static final String FILE_PATH = "filePath";
    public static final String DOCUMENT_ID = "documentId";
    public static final String TEST_PDF = "test.pdf";
    public static final String ID1 = "123";
    public static final String THE_DATA_SHOULD_NOT_BE_EMPTY = "The data should not be empty";
    public static final String SRC_TEST_RESOURCES_TEST_IMAGE_PNG = "src/test/resources/test-image.png";
    public static final String PATH_THAT_DOES_NOT_EXIST_FILE_PDF = "/path/that/does/not/exist/file.pdf";
    public static final String PATH = "/path/to/directory";
    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @InjectMocks
    private PDFConversionsService pdfEditConversionService;

    @TempDir
    Path tempDir;
    private File temporaryFile;


    @BeforeEach
    void setUp() throws IOException {
        temporaryFile = Files.createTempFile("testPdf", ".pdf").toFile();
        Files.writeString(temporaryFile.toPath(), "PDF content");

    }

    @Test
    void testConvertImageToPdfAndReturnResponse_FileNotFound() {
        String documentId = "nonexistent";
        when(fileMetadataRepository.findById(documentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pdfEditConversionService.convertImageToPdf(documentId));
    }


    @Test
    void testConvertImageToPdfAndReturnResponse_ImageReadFailure() {
        String documentId = "existing";
        FileMetadata mockFileMetadata = new FileMetadata();
        mockFileMetadata.setDirectoryName(SRC_TEST_RESOURCES_TEST_IMAGE_PNG);
        when(fileMetadataRepository.findById(documentId)).thenReturn(Optional.of(mockFileMetadata));

        assertThrows(ResourceNotFoundException.class, () -> pdfEditConversionService.convertImageToPdf(documentId));
    }

    @Test
    void testConvertPdfAsZip() throws Exception {

        String documentId = "testDocument";

        File tempFile = File.createTempFile("test", ".pdf");

        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            out.write("PDF content".getBytes(StandardCharsets.UTF_8));
        }

        FileMetadata mockMetadata = new FileMetadata();
        mockMetadata.setId(documentId);
        mockMetadata.setDirectoryName(tempFile.getPath());

        when(fileMetadataRepository.findById(anyString())).thenReturn(Optional.of(mockMetadata));

        try {
            GenericResponse<List<byte[]>> response = pdfEditConversionService.convertPdfAsZip(documentId);
            assertNotNull(response);
            assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), response.getStatus());
            assertFalse(response.getData().isEmpty(), THE_DATA_SHOULD_NOT_BE_EMPTY);
        } finally {
            tempFile.delete();
        }
    }


    @Test
    void testConvertPdfAsZipThrowsFailedToCompressResourcesException() {

        when(fileMetadataRepository.findById(anyString())).thenReturn(Optional.of(new FileMetadata() {
            @Override
            public String getDirectoryName() {
                return PATH_THAT_DOES_NOT_EXIST_FILE_PDF;
            }
        }));

        assertThrows(FailedToCompressResourcesException.class, () -> {
            pdfEditConversionService.convertPdfAsZip(DOCUMENT_ID);
        });
    }

    @Test
    void testServiceMethod() {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId("1");
        fileMetadata.setFileName("sample.pdf");
        fileMetadata.setDirectoryName(PATH);

        when(fileMetadataRepository.findById(anyString())).thenReturn(Optional.of(fileMetadata));

        File response = pdfEditConversionService.getFileById("1");

        assertNotNull(response);
    }


    @Test
    void testConvertPdfToWordBytes_FileMetadataRepositoryReturnsAbsent() {
        when(fileMetadataRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pdfEditConversionService.convertPdfToWordBytes(DOCUMENT_ID)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testConvertPdfToWordBytes(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }

        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<List<byte[]>> result = pdfEditConversionService.convertPdfToWordBytes(ID);
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
        assertNotNull(result.getMessage());
        assertFalse(result.getData().isEmpty());
    }

    @Test
    void testConvertToJpegOrPng() throws Exception {

        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }

        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<List<byte[]>> result = pdfEditConversionService.convertPdfToJpegOrPng(ID, "jpeg", 1, 2);
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    void testConvertPdfPagesToTextFile(@TempDir Path tempDir) throws Exception {
        String format = "txt";
        int page = 1;
        int size = 10;
        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }
        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<List<byte[]>> result = pdfEditConversionService.conversion(ID, format, page, size);
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
        assertFalse(result.getData().isEmpty());
    }

    @Test
    void testSplitPDFSingle(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }
        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        // Run the test
        final GenericResponse<List<byte[]>> result = pdfEditConversionService.splitPDF(ID, "single", "3");
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    void testSplitPDFRange(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }
        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<List<byte[]>> result = pdfEditConversionService.splitPDF(ID, "range", "3-6");
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    void testSplitPDFRandom(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }
        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<List<byte[]>> result = pdfEditConversionService.splitPDF(ID, "random", "3,6");
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    void testSplitPDFAndConvertPagesToText_FileMetadataRepositoryReturnsAbsent() {
        when(fileMetadataRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pdfEditConversionService.splitPDF(DOCUMENT_ID, "splitType", "splitNumbers")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testConvertImageToPdfAndReturnResponse_FileMetadataRepositoryReturnsAbsent() {
        when(fileMetadataRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pdfEditConversionService.convertImageToPdf(DOCUMENT_ID)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testRotatePDF(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }
        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<byte[]> result = pdfEditConversionService
                .rotatePDF(ID);
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    void testConversion(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }

        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<List<byte[]>> result1 = pdfEditConversionService.conversion(ID, "ZIP", 0, 0);
        final GenericResponse<List<byte[]>> result2 = pdfEditConversionService.conversion(ID, "png", 0, 0);
        final GenericResponse<List<byte[]>> result4 = pdfEditConversionService.conversion(ID, "doc", 0, 0);
        final GenericResponse<List<byte[]>> result5 = pdfEditConversionService.conversion(ID, "txt", 0, 0);
        final GenericResponse<List<byte[]>> result3 = pdfEditConversionService.conversion(ID, "docx", 0, 0);
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result4);
        assertNotNull(result5);
        assertNotNull(result3);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result1.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result1.getMessage());
        assertNotNull(result1.getData());
    }

    @Test
    void testCompressPDFAndReturnResponse(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve(TEST_PDF).toFile();
        PDResources resources = new PDResources();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            page.setResources(resources);
            doc.addPage(page);
            doc.save(file);

        }

        FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID1);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        when(fileMetadataRepository.findById(anyString())).thenReturn(Optional.of(fileMetadata1));

        GenericResponse<byte[]> response = pdfEditConversionService.compressPDF(ID1);

        assertNotNull(response);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), response.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), response.getMessage());
        assertNotNull(response.getData());


        verify(fileMetadataRepository).findById(ID1);


    }

    @Test
    void testCompressImage() throws IOException {
        PDDocument document = new PDDocument();
        try {
            PDPage page = new PDPage();
            document.addPage(page);
            PDResources resources = new PDResources();
            page.setResources(resources);
            BufferedImage bufferedImage = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
            PDImageXObject image = mock(PDImageXObject.class);
            COSName name = COSName.getPDFName("testImage");
            when(image.getImage()).thenReturn(bufferedImage);


            pdfEditConversionService.compressImage(document, resources, name, image);
            assertNotNull(resources.getXObject(name), "Image should be added to resources.");
        } finally {
            if (document == null) {
                document.close();
            }
        }
    }


    @Test
    void testRotatePdf(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }
        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        // Run the test
        final GenericResponse<byte[]> result = pdfEditConversionService.rotatePDF(ID);
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    void testConvertImageToPdfAndReturnResponse_PositiveCase(@TempDir Path tempDir) throws IOException {
        File imageFile = tempDir.resolve("test.jpg").toFile();
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "jpg", imageFile);

        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName("test.jpg");
        fileMetadata1.setFilePath(imageFile.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        fileMetadata1.setDirectoryName(imageFile.getAbsolutePath());
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);
        final GenericResponse<byte[]> response = pdfEditConversionService.convertImageToPdf(ID);

        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), response.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void testRotatePDFAndReturnResponse_FileMetadataRepositoryReturnsAbsent() {
        when(fileMetadataRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pdfEditConversionService.rotatePDF(DOCUMENT_ID)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rotatePDFAndReturnResponse_shouldThrowIOException_whenDocumentIdIsNull() {
        FileMetadata mockFileMetadata = new FileMetadata();
        mockFileMetadata.setFileName(temporaryFile.getName());
        mockFileMetadata.setFilePath(temporaryFile.getAbsolutePath());
        mockFileMetadata.setDirectoryName(temporaryFile.getParent());
        mockFileMetadata.setDocumentId(DOCUMENT_ID);
        when(fileMetadataRepository.findById(anyString())).thenReturn(Optional.of(mockFileMetadata));
        assertThrows(PotentialStubbingProblem.class, () -> pdfEditConversionService.rotatePDF(null));
    }

    @Test
    void testRemovePdfSingle(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }
        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<List<byte[]>> result = pdfEditConversionService.removePagesFromPDF(ID, "single", "6");
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    void testRemovePDFRange(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }
        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<List<byte[]>> result = pdfEditConversionService.removePagesFromPDF(ID, "range", "6-8");
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    void testRemovePDFRandom(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }
        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<List<byte[]>> result = pdfEditConversionService.removePagesFromPDF(ID, "random", "6,8");
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    void testRemovePagesFromPDF_FileMetadataRepositoryReturnsAbsent() {
        when(fileMetadataRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pdfEditConversionService.removePagesFromPDF(DOCUMENT_ID, "removeType", "removeNumber")).isInstanceOf(ResourceNotFoundException.class);
    }


    @Test
    void testCompressPDFAndReturnResponse_FileMetadataRepositoryReturnsAbsent() {
        when(fileMetadataRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pdfEditConversionService.compressPDF(DOCUMENT_ID)).isInstanceOf(ResourceNotFoundException.class);
    }
    @Test
    void convertXlsToPdf_Success(@TempDir Path tempDir) throws Exception {
        File xlsFile = tempDir.resolve("test.xlsx").toFile();
        try (Workbook workbook = new XSSFWorkbook(); // Using XSSFWorkbook for the test
             FileOutputStream out = new FileOutputStream(xlsFile)) {
            workbook.createSheet("Test");
            workbook.write(out);
        }
        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(xlsFile.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(anyString())).thenReturn(fileMetadata);

        // Execute
        GenericResponse<byte[]> response = pdfEditConversionService.convertXlsToPdf("id");

        // Verify
        assertNotNull(response);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), response.getStatus());
        assertTrue(response.getData().length > 0);
    }

    @Test
    void testConvertTiffToPdfAndReturnResponse(@TempDir Path tempDir) throws IOException {
        File tiffFile = tempDir.resolve("test.tiff").toFile();
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "tiff", tiffFile);

        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName("test.tiff");
        fileMetadata1.setFilePath(tiffFile.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        fileMetadata1.setDirectoryName(tiffFile.getAbsolutePath());
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);
        final GenericResponse<byte[]> response = pdfEditConversionService.convertTiffToPdf(ID);

        assertNotNull(response);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), response.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void testconvertPdfToDocxBytes() throws IOException{
        File sourceFile=tempDir.resolve("test.pdf").toFile();
        try(PDDocument document=new PDDocument()){
            document.addPage(new PDPage());
            document.save(sourceFile);
        }

        final FileMetadata fileMetadata1=new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(sourceFile.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);

        final Optional<FileMetadata> fileMetadata=Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(anyString())).thenReturn(fileMetadata);

        GenericResponse<List<byte[]>> result=pdfEditConversionService.convertPdfToDocxBytes(DOCUMENT_ID);

        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(),result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),result.getMessage());
        assertNotNull(result.getData());
    }
    @Test
    void convertTextToPdf_Success(@TempDir Path tempDir) throws IOException {
        File tempFile = tempDir.resolve("test.txt").toFile();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("Hello, PDF!");
        }
        final FileMetadata fileMetadata1=new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(tempFile.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);

        final Optional<FileMetadata> fileMetadata=Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(anyString())).thenReturn(fileMetadata);

        final GenericResponse<byte[]> response = pdfEditConversionService.convertTextToPdf(DOCUMENT_ID);

        assertNotNull(response);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), response.getStatus());
        assertTrue(response.getData().length > 0);
    }

    @Test
    void testConvertToTiff() throws Exception {

        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }

        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<List<byte[]>> result = pdfEditConversionService.convertPdfToTiff(ID, 1, 2);
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    void testConvertToXlsx() throws Exception {

        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }

        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<List<byte[]>> result = pdfEditConversionService.convertPdfToXlsxBytes(ID);
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
    }
    @Test
    void testPdfMerger() throws Exception {

        File file = tempDir.resolve(TEST_PDF).toFile();

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(file);
        }

        final FileMetadata fileMetadata1 = new FileMetadata();
        fileMetadata1.setId(ID);
        fileMetadata1.setFileName(FILE_NAME);
        fileMetadata1.setFilePath(FILE_PATH);
        fileMetadata1.setDirectoryName(file.getAbsolutePath());
        fileMetadata1.setDocumentId(DOCUMENT_ID);
        final Optional<FileMetadata> fileMetadata = Optional.of(fileMetadata1);
        when(fileMetadataRepository.findById(ID)).thenReturn(fileMetadata);

        final GenericResponse<byte[]> result = pdfEditConversionService.mergePdfDocuments(ID);
        assertNotNull(result);
        assertEquals(Constants.RESPONSE_STATUS.OK.getValue(), result.getStatus());
        assertEquals(Constants.RESPONSE_MESSAGE.SUCCESS.getValue(), result.getMessage());
        assertNotNull(result.getData());
    }
}
