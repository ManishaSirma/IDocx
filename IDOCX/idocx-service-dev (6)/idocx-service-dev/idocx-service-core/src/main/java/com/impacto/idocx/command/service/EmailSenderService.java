package com.impacto.idocx.command.service;

import com.impacto.idocx.command.dtos.EmailRequestDto;
import com.impacto.idocx.command.utils.SendGridEmailUtils;
import com.sendgrid.Response;
import com.sendgrid.helpers.mail.objects.Attachments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EmailSenderService implements NotificationSenderInterface {
    private final SendGridEmailUtils sendGridEmailUtils;

    @Override
    public Response sendEmailWithAttachments(EmailRequestDto emailRequestDto) throws IOException {
        String attachmentContent = encodeFileToBase64Binary(Paths.get(emailRequestDto.getFilePath()));
        Attachments attachment = this.createAttachment(attachmentContent, "application/octet-stream", Paths.get(emailRequestDto.getFilePath()).getFileName().toString(), "attachment", "file");
       return this.sendGridEmailUtils.sendEmailWithAttachment(emailRequestDto.getFromEmail(),
                emailRequestDto.getToEmail(),
                emailRequestDto.getSubject(),
                emailRequestDto.getBody(), attachment);
    }

    public String encodeFileToBase64Binary(Path path) throws IOException {
        byte[] fileContent = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(fileContent);
    }

    public Attachments createAttachment(String content, String type, String filename, String disposition, String contentId) {
        Attachments attachment = new Attachments();
        attachment.setContent(content);
        attachment.setType(type);
        attachment.setFilename(filename);
        attachment.setDisposition(disposition);
        if (contentId != null) {
            attachment.setContentId(contentId);
        }
        return attachment;
    }
}
