package com.impacto.idocx.command.api;

import com.impacto.idocx.command.dtos.EmailRequestDto;
import com.impacto.idocx.command.service.NotificationSenderInterface;
import com.sendgrid.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationSenderInterface mockNotificationSenderInterface;

    @Test
    void testSendEmailWithAttachments() throws Exception {

        when(mockNotificationSenderInterface.sendEmailWithAttachments(
                new EmailRequestDto("fromEmail", "toEmail", "body", "subject", "filePath"))).thenReturn(new Response());

        final MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(post("/v1/send-email")
                        .content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(mockHttpServletResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value()); //TODO need to change after payment of send grid is done

    }

    @Test
    void testSendEmailWithAttachments_NotificationSenderInterfaceThrowsIOException() throws Exception {
        when(mockNotificationSenderInterface.sendEmailWithAttachments(
                new EmailRequestDto("fromEmail", "toEmail", "body", "subject", "filePath")))
                .thenThrow(IOException.class);

        final MockHttpServletResponse response = mockMvc.perform(post("/v1/send-email")
                        .content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value()); //TODO need to change after payment of send grid is done
    }
    @Test
    void testSendEmailWithAttachment() throws IOException {
        MockitoAnnotations.openMocks(this);
        NotificationController notificationController = new NotificationController(mockNotificationSenderInterface);

        EmailRequestDto emailRequestDto = new EmailRequestDto("from@example.com", "to@example.com", "Hello", "Test email", "attachment.txt");
        notificationController.sendEmailWithAttachments(emailRequestDto);

        verify(mockNotificationSenderInterface).sendEmailWithAttachments(emailRequestDto);
    }
}
