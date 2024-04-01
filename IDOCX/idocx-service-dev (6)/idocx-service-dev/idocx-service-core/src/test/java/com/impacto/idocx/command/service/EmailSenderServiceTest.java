package com.impacto.idocx.command.service;

import com.impacto.idocx.command.dtos.EmailRequestDto;
import com.impacto.idocx.command.utils.SendGridEmailUtils;
import com.sendgrid.Response;
import com.sendgrid.helpers.mail.objects.Attachments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceTest {

    @Mock
    private SendGridEmailUtils mockSendGridEmailUtils;

    private EmailSenderService emailSenderServiceUnderTest;

    @BeforeEach
    void setUp() {
        emailSenderServiceUnderTest = new EmailSenderService(mockSendGridEmailUtils);
    }

    @Test
    void testSendEmailWithAttachments() throws Exception {
        Path tempFilePath = createTempFileWithContent();
        final EmailRequestDto emailRequestDto = new EmailRequestDto("fromEmail", "toEmail", "body", "subject",
                tempFilePath.toString());

        final Attachments attachment = new Attachments();
        attachment.setContent("content");
        attachment.setType("type");
        attachment.setFilename("filename");
        attachment.setDisposition("disposition");
        attachment.setContentId("contentId");
        when(mockSendGridEmailUtils.sendEmailWithAttachment(any(), any(), any(), any(),
                any())).thenReturn(new Response());

        final Response result = emailSenderServiceUnderTest.sendEmailWithAttachments(emailRequestDto);
        assertNotNull(result);
        Files.deleteIfExists(tempFilePath);

    }

    @Test
    void testSendEmailWithAttachments_SendGridEmailUtilsThrowsIOException() throws Exception {
        Path tempFilePath = createTempFileWithContent();
        final EmailRequestDto emailRequestDto = new EmailRequestDto("fromEmail", "toEmail", "body", "subject",
                tempFilePath.toString());

        when(mockSendGridEmailUtils.sendEmailWithAttachment(any(), any(), any(), any(), any())).thenThrow(IOException.class);
        assertThatThrownBy(() -> emailSenderServiceUnderTest.sendEmailWithAttachments(emailRequestDto))
                .isInstanceOf(IOException.class);
        Files.deleteIfExists(tempFilePath);
    }

    @Test
    void testEncodeFileToBase64Binary() throws Exception {
        Path tempFilePath = createTempFileWithContent();
        assertThat(emailSenderServiceUnderTest.encodeFileToBase64Binary(Paths.get(tempFilePath.toString())))
                .isEqualTo("VGhpcyBpcyB0aGUgY29udGVudCBvZiB0aGUgdGVtcCBmaWxlLg==");

        assertThatThrownBy(() -> emailSenderServiceUnderTest.encodeFileToBase64Binary(Paths.get("nonexistent_file.txt")))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("nonexistent_file.txt");

        Files.deleteIfExists(tempFilePath);

    }

    @Test
    void testCreateAttachment() {
        // Setup
        final Attachments expectedResult = new Attachments();
        expectedResult.setContent("content");
        expectedResult.setType("type");
        expectedResult.setFilename("filename");
        expectedResult.setDisposition("disposition");
        expectedResult.setContentId("contentId");

        final Attachments result = emailSenderServiceUnderTest.createAttachment("content", "type", "filename",
                "disposition", "contentId");
        assertThat(result).isEqualTo(expectedResult);
    }
    public static Path createTempFileWithContent() throws IOException {
        Path tempFile = Files.createTempFile("tempFileToSend", ".txt");
        String content = "This is the content of the temp file.";
        Files.write(tempFile, content.getBytes());
        return tempFile;
    }
}
