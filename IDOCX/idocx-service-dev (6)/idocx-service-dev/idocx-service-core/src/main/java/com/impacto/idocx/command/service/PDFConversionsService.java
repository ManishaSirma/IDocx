package com.impacto.idocx.command.service;

import com.impacto.idocx.command.common.Constants;
import com.impacto.idocx.command.common.GenericResponse;
import com.impacto.idocx.command.dao.FileMetadataRepository;
import com.impacto.idocx.command.entity.FileMetadata;
import com.impacto.idocx.command.exceptions.ErrorCode;
import com.impacto.idocx.command.exceptions.FailedToCompressResourcesException;
import com.impacto.idocx.command.exceptions.FileReadingException;
import com.impacto.idocx.command.exceptions.InvalidExcelFormatException;
import com.impacto.idocx.command.exceptions.LimitExceedingException;
import com.impacto.idocx.command.exceptions.ResourceNotFoundException;
import com.impacto.idocx.command.exceptions.UnsupportedException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class PDFConversionsService {
    public static final String SINGLE = "Single";
    public static final String RANGE = "Range";
    public static final String RANDOM = "Random";
    private static final int MAX_RESOLUTION = 150;
    public static final int DPI = 300;

    private final FileMetadataRepository fileMetadataRepository;

    public GenericResponse<List<byte[]>> conversion(String documentId, String format, int page, int size) throws IOException {
        if (!Arrays.stream(Constants.EXTENSIONS.values())
                .anyMatch(enumValue -> enumValue.name().equals(format.toUpperCase())))
            throw new UnsupportedException(ErrorCode.UNSUPPORTED_EXCEPTION, "Unsupported Extension format: " + format);
        return switch (Constants.EXTENSIONS.valueOf(format.toUpperCase())) {
            case ZIP -> convertPdfAsZip(documentId);
            case DOC -> convertPdfToWordBytes(documentId);
            case TXT -> convertPdfPagesToTextFile(documentId, page, size);
            case DOCX -> convertPdfToDocxBytes(documentId);
            case TIFF -> convertPdfToTiff(documentId, page, size);
            case XLSX -> convertPdfToXlsxBytes(documentId);
            default -> convertPdfToJpegOrPng(documentId, format.toUpperCase(), page, size);
        };
    }

    public File getFileById(String documentId) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND_EXCEPTION, "File with id: " + documentId + " not found"));
        Path filePath = Paths.get(fileMetadata.getDirectoryName());
        return new File(filePath.toUri());
    }

    public GenericResponse<List<byte[]>> convertPdfAsZip(String documentId) {
        try {
            File sourceFile = getFileById(documentId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            FileInputStream fis = new FileInputStream(sourceFile);
            ZipEntry zipEntry = new ZipEntry(sourceFile.getName());
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

            zos.closeEntry();
            fis.close();
            zos.close();

            return new GenericResponse<>(
                    Constants.RESPONSE_STATUS.OK.getValue(),
                    Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                    Arrays.asList(baos.toByteArray()), 1, 1, 1);
        } catch (IOException e) {
            throw new FailedToCompressResourcesException(ErrorCode.FAILED_TO_COMPRESS_RESOURCE_EXCEPTION, "Failed to compress PDF");
        }
    }

    public GenericResponse<List<byte[]>> convertPdfToWordBytes(String documentId) throws IOException {
        File sourceFile = getFileById(documentId);
        List<byte[]> docsFilesAsBytes = new ArrayList<>();

        try (PDDocument pdfDocument = Loader.loadPDF(sourceFile)) {
            ByteArrayOutputStream out;
            try (XWPFDocument docxDocument = new XWPFDocument()) {
                out = new ByteArrayOutputStream();
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pdfDocument);
                docxDocument.createParagraph().createRun().setText(text);
                docxDocument.write(out);
            }

            docsFilesAsBytes.add(out.toByteArray());
        }
        return new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                docsFilesAsBytes
        );
    }

    public GenericResponse<List<byte[]>> convertPdfPagesToTextFile(String documentId, int page, int size) throws IOException {
        File sourceFile = getFileById(documentId);
        List<byte[]> txtFilesAsBytes = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(sourceFile)) {
            int documentPages = document.getNumberOfPages();
            if (size == -1) size = documentPages;
            int startPage = Math.max(page - 1, 0);
            int endPage = Math.min(startPage + size, documentPages);

            PDFTextStripper textStripper = new PDFTextStripper();

            for (int pageIndex = startPage; pageIndex < endPage; pageIndex++) {
                textStripper.setStartPage(pageIndex + 1);
                textStripper.setEndPage(pageIndex + 1);
                String pageText = textStripper.getText(document);

                byte[] pageTextBytes = pageText.getBytes(StandardCharsets.UTF_8);
                txtFilesAsBytes.add(pageTextBytes);
            }

            return new GenericResponse<>(
                    Constants.RESPONSE_STATUS.OK.getValue(),
                    Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                    txtFilesAsBytes,
                    documentPages,
                    endPage - startPage,
                    page
            );
        }
    }

    public GenericResponse<List<byte[]>> convertPdfToJpegOrPng(String documentId, String format, int page, int size) throws IOException {
        File sourceFile = getFileById(documentId);
        List<byte[]> images = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(sourceFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int documentPages = document.getNumberOfPages();
            if (size == -1) size = documentPages;
            int startPage = Math.max(page - 1, 0);
            int endPage = Math.min(startPage + size, documentPages);

            for (int pageIndex = startPage; pageIndex < endPage; pageIndex++) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(pageIndex, DPI);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bim, format, baos);
                images.add(baos.toByteArray());
            }
            return new GenericResponse<>(
                    Constants.RESPONSE_STATUS.OK.getValue(),
                    Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                    images,
                    documentPages,
                    endPage - startPage,
                    page
            );
        }
    }

    public GenericResponse<List<byte[]>> splitPDF(String documentId, String splitType, String splitNumbers) throws IOException {
        File sourceFile = getFileById(documentId);

        List<Integer> pagesToExtract = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(sourceFile)) {
            determinePagesToExtract(splitType, splitNumbers, document, pagesToExtract);

            List<byte[]> txtFilesAsBytes = extractTextFromPages(document, pagesToExtract);

            return new GenericResponse<>(
                    Constants.RESPONSE_STATUS.OK.getValue(),
                    Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                    txtFilesAsBytes,
                    document.getNumberOfPages(),
                    txtFilesAsBytes.size(),
                    pagesToExtract.size()
            );
        }
    }

    private void determinePagesToExtract(String splitType, String splitNumbers, PDDocument document, List<Integer> pagesToExtract) {
        if (SINGLE.equalsIgnoreCase(splitType)) {
            extractedSplitSingle(splitNumbers, document, pagesToExtract);
        } else if (RANGE.equalsIgnoreCase(splitType)) {
            extractedSplitRange(splitNumbers, document, pagesToExtract);
        } else if (RANDOM.equalsIgnoreCase(splitType)) {
            extractedSplitRandom(splitNumbers, document, pagesToExtract);
        }
    }

    private void extractedSplitSingle(String splitNumber, PDDocument document, List<Integer> pagesToExtract) {
        int pageNumber = Integer.parseInt(splitNumber.trim());
        if (pageNumber >= 1 && pageNumber <= document.getNumberOfPages()) {
            pagesToExtract.add(pageNumber);
        }
    }

    private void extractedSplitRange(String splitNumbers, PDDocument document, List<Integer> pagesToExtract) {
        String[] range = splitNumbers.split("-");
        int startPage = Integer.parseInt(range[0].trim());
        int endPage = Integer.parseInt(range[1].trim());
        if (startPage >= 1 && endPage <= document.getNumberOfPages()) {
            for (int i = startPage; i <= endPage; i++) {
                pagesToExtract.add(i);
            }
        }
    }

    private void extractedSplitRandom(String splitNumbers, PDDocument document, List<Integer> pagesToExtract) {
        String[] numbers = splitNumbers.split(",");
        Arrays.sort(numbers, Comparator.comparingInt(Integer::parseInt));
        for (String number : numbers) {
            int pageNum = Integer.parseInt(number.trim());
            if (pageNum >= 1 && pageNum <= document.getNumberOfPages()) {
                pagesToExtract.add(pageNum);
            }
        }
    }

    private List<byte[]> extractTextFromPages(PDDocument document, List<Integer> pagesToExtract) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        List<byte[]> txtFilesAsBytes = new ArrayList<>();

        for (Integer pageNumber : pagesToExtract) {
            textStripper.setStartPage(pageNumber);
            textStripper.setEndPage(pageNumber);
            String pageText = textStripper.getText(document);

            byte[] pageTextBytes = pageText.getBytes(StandardCharsets.UTF_8);
            txtFilesAsBytes.add(pageTextBytes);
        }

        return txtFilesAsBytes;
    }


    public GenericResponse<byte[]> convertImageToPdf(String documentId) throws IOException {
        File sourceFile = getFileById(documentId);
        if (!sourceFile.exists())
            throw new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND_EXCEPTION, "File not exist with this documentID. " + documentId);

        BufferedImage image = ImageIO.read(sourceFile);
        if (image == null) {
            throw new FileReadingException(ErrorCode.FILE_READING_EXCEPTION, "The file could not be opened as an image: " + sourceFile);
        }

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
            document.addPage(page);
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, 0, 0);
            }

            document.save(out);
            byte[] pdfBytes = out.toByteArray();

            return new GenericResponse<>(
                    Constants.RESPONSE_STATUS.OK.getValue(),
                    Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                    pdfBytes, 1, 1, 1
            );
        }
    }

    public GenericResponse<byte[]> rotatePDF(String documentId) throws IOException {
        File sourceFile = getFileById(documentId);
        byte[] rotatedPdfBytes;
        int totalOriginalPages;

        try (PDDocument document = Loader.loadPDF(sourceFile)) {
            totalOriginalPages = document.getNumberOfPages();
            document.getPages().forEach(page -> page.setRotation(90));

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.save(outputStream);
                rotatedPdfBytes = outputStream.toByteArray();
            }
        }

        return new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                rotatedPdfBytes, totalOriginalPages, 1, 0
        );
    }

    public GenericResponse<List<byte[]>> removePagesFromPDF(String documentId, String removeType, String removeNumber) throws IOException {
        File sourceFile = getFileById(documentId);
        List<Integer> pagesToRemove = new ArrayList<>();
        List<byte[]> removedPagesAsBytes = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(sourceFile)) {
            if (SINGLE.equalsIgnoreCase(removeType)) {
                extractedRemoveSingle(removeNumber, document, pagesToRemove);
            } else if (RANGE.equalsIgnoreCase(removeType)) {
                extractedRemoveRange(removeNumber, document, pagesToRemove);
            } else if (RANDOM.equalsIgnoreCase(removeType)) {
                extractedRemoveRandom(removeNumber, document, pagesToRemove);
            }

            byte[] modifiedDocumentBytes = getModifiedDocumentBytes(pagesToRemove, document);
            removedPagesAsBytes.add(modifiedDocumentBytes);

            return new GenericResponse<>(
                    Constants.RESPONSE_STATUS.OK.getValue(),
                    Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                    removedPagesAsBytes, document.getNumberOfPages(),
                    document.getNumberOfPages() - pagesToRemove.size(), 0
            );
        }
    }

    private void extractedRemoveSingle(String removeNumber, PDDocument document, List<Integer> pagesToRemove) {
        int pageNumber = Integer.parseInt(removeNumber.trim());
        if (pageNumber >= 1 && pageNumber <= document.getNumberOfPages()) {
            pagesToRemove.add(pageNumber);
        }
    }


    private byte[] getModifiedDocumentBytes(List<Integer> pagesToRemove, PDDocument originalDocument) throws IOException {
        try (PDDocument modifiedDocument = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (int i = 0; i < originalDocument.getNumberOfPages(); i++) {
                if (!pagesToRemove.contains(i + 1)) {
                    PDPage page = originalDocument.getPage(i);
                    modifiedDocument.addPage(page);
                }
            }
            modifiedDocument.save(outputStream);
            return outputStream.toByteArray();
        }
    }


    private void extractedRemoveRandom(String removeNumber, PDDocument document, List<Integer> pagesToRemove) {
        String[] numbers = removeNumber.split(",");
        Arrays.sort(numbers, Comparator.reverseOrder());
        for (String number : numbers) {
            extractedRemoveSingle(number, document, pagesToRemove);
        }
    }

    private void extractedRemoveRange(String removeNumber, PDDocument document, List<Integer> pagesToRemove) {
        String[] range = removeNumber.split("-");
        int startPage = Integer.parseInt(range[0].trim());
        int endPage = Integer.parseInt(range[1].trim());
        if (startPage >= 1 && endPage <= document.getNumberOfPages() && startPage <= endPage) {
            for (int i = startPage; i <= endPage; i++) {
                pagesToRemove.add(i);
            }
        }
    }


    public GenericResponse<byte[]> compressPDF(String documentId) throws IOException {
        File sourceFile = getFileById(documentId);
        byte[] compressedPdfBytes;
        int totalOriginalPages;
        try (PDDocument document = Loader.loadPDF(sourceFile)) {
            totalOriginalPages = document.getNumberOfPages();
            compressImages(document);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.save(outputStream); // Save the compressed document to a byte array
                compressedPdfBytes = outputStream.toByteArray();
            }
        }

        return new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                compressedPdfBytes,
                totalOriginalPages,
                1,
                0
        );
    }

    public void compressImages(PDDocument document) throws IOException {
        for (PDPage page : document.getPages()) {
            PDResources resources = page.getResources();
            for (COSName name : resources.getXObjectNames()) {
                PDXObject xobject = resources.getXObject(name);
                if (xobject instanceof PDImageXObject) {
                    PDImageXObject image = (PDImageXObject) xobject;
                    if (shouldCompress(image)) {
                        compressImage(document, resources, name, image);
                    }
                }
            }
        }
    }

    public boolean shouldCompress(PDImageXObject image) {
        return image.getWidth() > MAX_RESOLUTION || image.getHeight() > MAX_RESOLUTION;
    }

    public void compressImage(PDDocument document, PDResources resources, COSName name, PDImageXObject image) throws IOException {
        BufferedImage bufferedImage = image.getImage();
        double aspectRatio = (double) bufferedImage.getHeight() / bufferedImage.getWidth();
        int newWidth = MAX_RESOLUTION;
        int newHeight = (int) (MAX_RESOLUTION * aspectRatio);

        BufferedImage resizedBufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedBufferedImage.createGraphics();
        graphics2D.drawImage(bufferedImage, 0, 0, newWidth, newHeight, null);
        graphics2D.dispose();

        PDImageXObject compressedImage = LosslessFactory.createFromImage(document, resizedBufferedImage);
        resources.put(name, compressedImage);
    }

    public GenericResponse<byte[]> convertTiffToPdf(String documentId) throws IOException {
        File sourceFile = getFileById(documentId);


        BufferedImage image = ImageIO.read(sourceFile);
        if (image == null) {
            throw new FileReadingException(ErrorCode.FILE_READING_EXCEPTION, "The file could not be opened as an image: " + sourceFile);
        }

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
            document.addPage(page);
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, 0, 0);
            }

            document.save(out);
            byte[] pdfBytes = out.toByteArray();

            return new GenericResponse<>(
                    Constants.RESPONSE_STATUS.OK.getValue(),
                    Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                    pdfBytes, 1, 1, 1
            );
        }
    }


    public GenericResponse<byte[]> convertTextToPdf(String documentId) throws IOException {
        File sourceFile = getFileById(documentId);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page);
                 BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(25, 725);

                String line;
                while ((line = br.readLine()) != null) {
                    contentStream.showText(line);
                    contentStream.newLine();
                }
                contentStream.endText();
            }

            document.save(out);
            byte[] pdfBytes = out.toByteArray();

            return new GenericResponse<>(
                    Constants.RESPONSE_STATUS.OK.getValue(),
                    Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                    pdfBytes, 1, 1, 1
            );
        }
    }

    public GenericResponse<byte[]> convertXlsToPdf(String documentId) throws IOException {
        File xlsFile = getFileById(documentId);

        try (FileInputStream inputStream = new FileInputStream(xlsFile);
             Workbook workbook = WorkbookFactory.create(inputStream);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            workbookToPdf(workbook, out);

            byte[] pdfBytes = out.toByteArray();

            return new GenericResponse<>(
                    Constants.RESPONSE_STATUS.OK.getValue(),
                    Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                    pdfBytes, 1, 1, 1
            );
        }
    }

    private void workbookToPdf(Workbook workbook, ByteArrayOutputStream out) throws IOException {
        if (workbook instanceof XSSFWorkbook) {
            XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
            xssfWorkbook.write(out);
        } else {
            throw new InvalidExcelFormatException(ErrorCode.INVALID_EXCEL_FORMAT_EXCEPTION ,"Unsupported Excel format");
        }
    }

    public GenericResponse<List<byte[]>> convertPdfToDocxBytes(String documentId) throws IOException {
        File sourceFile = getFileById(documentId);
        List<byte[]> docxFilesAsBytes = new ArrayList<>();

        try (PDDocument pdfDocument = Loader.loadPDF(sourceFile)) {
            try (XWPFDocument docxDocument = new XWPFDocument()) {

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pdfDocument);

                XWPFParagraph paragraph = docxDocument.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(text);

                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    docxDocument.write(outputStream);
                    byte[] docxContent = outputStream.toByteArray();
                    docxFilesAsBytes.add(docxContent);
                }
            }
        }

        return new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                docxFilesAsBytes
        );
    }


    public GenericResponse<List<byte[]>> convertPdfToTiff(String documentId, int page, int size) throws IOException {
        File sourceFile = getFileById(documentId);
        List<byte[]> images = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(sourceFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int documentPages = document.getNumberOfPages();
            if (size == -1) size = documentPages;
            int startPage = Math.max(page - 1, 0);
            int endPage = Math.min(startPage + size, documentPages);

            for (int pageIndex = startPage; pageIndex < endPage; pageIndex++) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(pageIndex, DPI);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bim, "TIFF", baos);
                byte[] tiffBytes = baos.toByteArray();
                if (tiffBytes.length <= 50 * 1024 * 1024) { // Less than 50MB
                    images.add(tiffBytes);
                } else {
                    throw new LimitExceedingException(ErrorCode.LIMIT_EXCEEDING_EXCEPTION,"Converted TIFF file exceeds 50MB limit.");
                }
            }
            return new GenericResponse<>(
                    Constants.RESPONSE_STATUS.OK.getValue(),
                    Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                    images,
                    documentPages,
                    endPage - startPage,
                    page
            );
        }
    }

    public GenericResponse<List<byte[]>> convertPdfToXlsxBytes(String documentId) throws IOException {
        File sourceFile = getFileById(documentId);
        List<byte[]> xlsxFilesAsBytes = new ArrayList<>();

        try (PDDocument pdfDocument = Loader.loadPDF(sourceFile)) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("PDF_Content");
                PDFTextStripper pdfStripper = new PDFTextStripper();
                int rowIndex = 0;

                for (String line : pdfStripper.getText(pdfDocument).split("\\r?\\n")) {
                    Row row = sheet.createRow(rowIndex++);
                    Cell cell = row.createCell(0);
                    cell.setCellValue(line);
                }

                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    workbook.write(outputStream);
                    byte[] xlsxContent = outputStream.toByteArray();
                    xlsxFilesAsBytes.add(xlsxContent);
                }
            }
        }

        return new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                xlsxFilesAsBytes
        );
    }

    public GenericResponse<byte[]> mergePdfDocuments(String documentIds) throws IOException {
        List<String> documentIdList = Arrays.asList(documentIds.split(","));
        List<File> sourceFiles = new ArrayList<>();
        for (String documentId : documentIdList) {
            sourceFiles.add(getFileById(documentId));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PDFMergerUtility mergerUtility = new PDFMergerUtility();
            mergerUtility.setDestinationStream(outputStream);
            for (File sourceFile : sourceFiles) {
                mergerUtility.addSource(sourceFile);
            }
            mergerUtility.mergeDocuments(null);

            byte[] mergedContent = outputStream.toByteArray();

            return new GenericResponse<>(
                    Constants.RESPONSE_STATUS.OK.getValue(),
                    Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                    mergedContent
            );
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
