package com.impacto.idocx.command.dtos;

import com.impacto.idocx.command.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetaDataDto {
    private String id;
    private String fileName;
    private String filePath;
    private String directoryName;
    private String documentId;
    private String extension;
    private boolean passwordProtected;
    private String tag;
    private int version;
    private String remarks;
    private String authorizer;
    private Constants.WORKSPACE_TYPE workSpaceType;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String createdBy;
    private String modifiedBy;
    private boolean isFavourite;
    private boolean isArchive;
    private boolean isTrash;
}