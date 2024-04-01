package com.impacto.idocx.command.api;

import com.impacto.idocx.command.common.GenericResponse;
import com.impacto.idocx.command.service.PDFConversionsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/conversions")
public class PDFConversionsController {

    private final PDFConversionsService pdfConversionsService;

    @Operation(summary = "Converts PDF to Format ",
            description = "Converts a PDF to specified document format by its ID.")
    @PostMapping("/pdf-file")
    public ResponseEntity<GenericResponse<List<byte[]>>> convertPdf(@RequestParam String documentId,
                                                                    @RequestParam(defaultValue = "JPEG") String format,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "-1") int size) throws IOException {
        return new ResponseEntity<>(pdfConversionsService.conversion(documentId, format, page, size), HttpStatus.OK);
    }

    @Operation(summary= "Split PDF",
            description= "Splits a PDF document into specified parts based on the provided range.")
    @PostMapping("/pdf-split")
    public ResponseEntity<GenericResponse<List<byte[]>>> splitPDF(@RequestParam String documentId,
                                                                  @RequestParam String type,
                                                                  @RequestParam String range) throws IOException {
        return new ResponseEntity<>(pdfConversionsService.splitPDF(documentId, type, range), HttpStatus.OK);
    }

    @Operation(summary = "Remove pages from PDF",
            description = "Remove specific pages from a PDF document based on specified criteria.")
    @PostMapping("/pdf-remove")
    public ResponseEntity<GenericResponse<List<byte[]>>> removePDF(@RequestParam String documentId,
                                                                   @RequestParam String type,
                                                                   @RequestParam String range) throws IOException {
        return new ResponseEntity<>(pdfConversionsService.removePagesFromPDF(documentId, type, range), HttpStatus.OK);
    }

    @Operation(summary = "Rotate PDF",
            description = "Rotate pages of a PDF document by 90 degrees clockwise.")
    @PostMapping("/pdf-rotate")
    public ResponseEntity<GenericResponse<byte[]>> rotatePDF(@RequestParam String documentId) throws IOException {
        return new ResponseEntity<>(pdfConversionsService.rotatePDF(documentId), HttpStatus.OK);
    }

    @Operation(summary = "Compress PDF",
            description = "Compresses the PDF specified by documentId.")
    @PostMapping("/pdf-compress")
    public ResponseEntity<GenericResponse<byte[]>> compressPDF(@RequestParam String documentId) throws IOException {
        return new ResponseEntity<>(pdfConversionsService.compressPDF(documentId), HttpStatus.OK);
    }

    @Operation(summary = "Converts image to PDF",
            description = "Converts the image specified by the document ID to a PDF document.")
    @PostMapping("/image-pdf")
    public ResponseEntity<GenericResponse<byte[]>> convertImage(@RequestParam String documentId) throws IOException {
        return new ResponseEntity<>(pdfConversionsService.convertImageToPdf(documentId), HttpStatus.OK);
    }

    @Operation(summary = "Convert TIFF to PDF",
            description = "Converts a TIFF image specified by documentId to PDF format.")
    @PostMapping("/tiff-pdf")
    public ResponseEntity<GenericResponse<byte[]>> convertTIFFToPDF(@RequestParam String documentId) throws IOException {
        return new ResponseEntity<>(pdfConversionsService.convertTiffToPdf(documentId), HttpStatus.OK);
    }

    @Operation(summary = "Convert Text to PDF",
            description = "Converts a text file to a PDF document.")
    @PostMapping("/txt-pdf")
    public ResponseEntity<GenericResponse<byte[]>> convertTxtToPDF(@RequestParam String documentId) throws IOException {
        return new ResponseEntity<>(pdfConversionsService.convertTextToPdf(documentId), HttpStatus.OK);
    }

    @Operation(summary = "Convert XLSX to PDF",
            description = "Converts a XLSX file to a PDF document.")
    @PostMapping("/xlsx-pdf")
    public ResponseEntity<GenericResponse<byte[]>> convertXLSToPDF(@RequestParam String documentId) throws IOException {
        return new ResponseEntity<>(pdfConversionsService.convertXlsToPdf(documentId), HttpStatus.OK);
    }

    @Operation(summary = "PDF merger",
            description = "Merging more than One pdf documents.")
    @PostMapping("/pdf-merger")
    public ResponseEntity<GenericResponse<byte[]>> pdfMerger(@RequestParam String documentId) throws IOException {
        return new ResponseEntity<>(pdfConversionsService.mergePdfDocuments(documentId), HttpStatus.OK);
    }
}
