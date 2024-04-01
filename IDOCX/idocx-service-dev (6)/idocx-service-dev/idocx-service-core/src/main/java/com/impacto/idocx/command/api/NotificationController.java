package com.impacto.idocx.command.api;

import com.impacto.idocx.command.dtos.EmailRequestDto;
import com.impacto.idocx.command.service.NotificationSenderInterface;
import com.sendgrid.Response;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationSenderInterface notificationSenderInterface;

    @Operation(summary = "Send Email with Attachments", description = "Send an email with attachments to the specified recipient.")
    @PostMapping("/send-email")
    public Response sendEmailWithAttachments(@RequestBody EmailRequestDto emailRequestDto) throws IOException {
        return notificationSenderInterface.sendEmailWithAttachments(emailRequestDto);

    }

}
