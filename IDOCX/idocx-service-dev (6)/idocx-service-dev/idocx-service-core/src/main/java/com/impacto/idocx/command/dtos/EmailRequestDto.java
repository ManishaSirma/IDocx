package com.impacto.idocx.command.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequestDto {
    private String fromEmail;
    private String toEmail;
    private String body;
    private String subject;
    private String filePath;
}
