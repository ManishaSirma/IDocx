package com.impacto.idocx.command.dtos;

import com.impacto.idocx.command.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderMetadataDto {
    private String id;
    private String folderName;
    private String folderPath;
    private Constants.WORKSPACE_TYPE workSpaceType;
    private boolean isFavourite;
    private boolean isArchive;
    private boolean isTrash;
}