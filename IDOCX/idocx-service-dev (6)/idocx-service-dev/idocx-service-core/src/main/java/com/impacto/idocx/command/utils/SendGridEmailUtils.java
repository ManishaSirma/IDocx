package com.impacto.idocx.command.utils;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SendGridEmailUtils {

    public Response sendEmailWithAttachment(String fromEmail, String toEmail, String subject, String body, Attachments attachment) throws IOException {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        mail.addAttachments(attachment);

        SendGrid sg = new SendGrid("0d56a9eba1ac55296cf95f306c63a3b2");
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            return sg.api(request);
        } catch (IOException ex) {
            System.err.println("Error sending email: " + ex.getMessage());
            throw ex;
        }
    }
}
