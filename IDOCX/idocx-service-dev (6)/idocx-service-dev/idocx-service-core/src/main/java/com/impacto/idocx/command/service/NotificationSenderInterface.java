package com.impacto.idocx.command.service;

import com.impacto.idocx.command.dtos.EmailRequestDto;
import com.sendgrid.Response;

import java.io.IOException;

public interface NotificationSenderInterface {
    Response sendEmailWithAttachments(EmailRequestDto emailRequestDto) throws IOException;

}
