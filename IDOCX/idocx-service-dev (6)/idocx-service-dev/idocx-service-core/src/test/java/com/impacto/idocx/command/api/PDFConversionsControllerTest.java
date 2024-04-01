package com.impacto.idocx.command.api;

import com.impacto.idocx.command.common.GenericResponse;
import com.impacto.idocx.command.service.PDFConversionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class PDFConversionsControllerTest {
    public static final String DOCUMENT_ID = "123";
    public static final String TYPE = "Type";
    public static final String RANGE = "Range";
    public static final String SUCCESS = "Success";
    public static final String JPEG = "JPEG";
    @Mock
    private PDFConversionsService pdfEditConversionService;

    @InjectMocks
    private PDFConversionsController pdfController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConvertPdf_Success() throws IOException {
        int page = 0;
        int size = -1;

        GenericResponse<List<byte[]>> response = new GenericResponse<>(HttpStatus.OK.value(), SUCCESS, null);
        when(pdfEditConversionService.conversion(anyString(), anyString(), anyInt(), anyInt())).thenReturn(response);

        ResponseEntity<GenericResponse<List<byte[]>>> entity = pdfController.convertPdf(JPEG, DOCUMENT_ID, page, size);

        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(response, entity.getBody());
    }

    @Test
    void testEditSplitPDF_Success() throws IOException {

        GenericResponse<List<byte[]>> response = new GenericResponse<>(HttpStatus.OK.value(), SUCCESS, null);
        when(pdfEditConversionService.splitPDF(anyString(), anyString(), anyString())).thenReturn(response);

        ResponseEntity<GenericResponse<List<byte[]>>> entity = pdfController.splitPDF(DOCUMENT_ID, TYPE, RANGE);

        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(response, entity.getBody());
    }

    @Test
    void testEditRemovePDF_Success() throws IOException {

        GenericResponse<List<byte[]>> response = new GenericResponse<>(HttpStatus.OK.value(), SUCCESS, null);
        when(pdfEditConversionService.removePagesFromPDF(anyString(), anyString(), anyString())).thenReturn(response);

        ResponseEntity<GenericResponse<List<byte[]>>> entity = pdfController.removePDF(DOCUMENT_ID, TYPE, RANGE);

        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(response, entity.getBody());
    }

    @Test
    void testEditRotatePDF_Success() throws IOException {

        GenericResponse<byte[]> response = new GenericResponse<>(HttpStatus.OK.value(), SUCCESS, null);
        when(pdfEditConversionService.rotatePDF(anyString())).thenReturn(response);

        ResponseEntity<GenericResponse<byte[]>> entity = pdfController.rotatePDF(DOCUMENT_ID);

        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(response, entity.getBody());
    }

    @Test
    void testEditCompressPDF_Success() throws IOException {

        GenericResponse<byte[]> response = new GenericResponse<>(HttpStatus.OK.value(), SUCCESS, null);
        when(pdfEditConversionService.compressPDF(anyString())).thenReturn(response);

        ResponseEntity<GenericResponse<byte[]>> entity = pdfController.compressPDF(DOCUMENT_ID);

        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(response, entity.getBody());
    }

    @Test
    void testConversionPdf_Success() throws IOException {

        GenericResponse<byte[]> response = new GenericResponse<>(HttpStatus.OK.value(), SUCCESS, null);
        when(pdfEditConversionService.convertImageToPdf(anyString())).thenReturn(response);

        ResponseEntity<GenericResponse<byte[]>> entity = pdfController.convertImage(DOCUMENT_ID);

        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(response, entity.getBody());
    }

    @Test
    void testTxtToPDF_Success() throws IOException {

        GenericResponse<byte[]> response = new GenericResponse<>(HttpStatus.OK.value(), SUCCESS, null);
        when(pdfEditConversionService.convertTextToPdf(anyString())).thenReturn(response);

        ResponseEntity<GenericResponse<byte[]>> entity = pdfController.convertTxtToPDF(DOCUMENT_ID);

        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(response, entity.getBody());
    }

    @Test
    void testXLSXToPDF_Success() throws IOException {

        GenericResponse<byte[]> response = new GenericResponse<>(HttpStatus.OK.value(), SUCCESS, null);
        when(pdfEditConversionService.convertXlsToPdf(anyString())).thenReturn(response);

        ResponseEntity<GenericResponse<byte[]>> entity = pdfController.convertXLSToPDF(DOCUMENT_ID);

        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(response, entity.getBody());
    }

    @Test
    void testTiffToPDF_Success() throws IOException {

        GenericResponse<byte[]> response = new GenericResponse<>(HttpStatus.OK.value(), SUCCESS, null);
        when(pdfEditConversionService.convertTiffToPdf(anyString())).thenReturn(response);

        ResponseEntity<GenericResponse<byte[]>> entity = pdfController.convertTIFFToPDF(DOCUMENT_ID);

        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(response, entity.getBody());
    }

    @Test
    void testPDFMerger_Success() throws IOException {

        GenericResponse<byte[]> response = new GenericResponse<>(HttpStatus.OK.value(), SUCCESS, null);
        when(pdfEditConversionService.mergePdfDocuments(anyString())).thenReturn(response);

        ResponseEntity<GenericResponse<byte[]>> entity = pdfController.pdfMerger(DOCUMENT_ID);

        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(response, entity.getBody());
    }

}