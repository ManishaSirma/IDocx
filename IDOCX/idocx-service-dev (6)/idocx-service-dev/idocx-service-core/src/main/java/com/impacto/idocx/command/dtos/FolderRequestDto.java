package com.impacto.idocx.command.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderRequestDto {
    private String folderPath;
    private String folderName;
}
