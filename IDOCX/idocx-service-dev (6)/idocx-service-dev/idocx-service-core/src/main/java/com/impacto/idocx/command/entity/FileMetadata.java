package com.impacto.idocx.command.entity;

import com.impacto.idocx.command.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "fileMetadata")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {

    @Id
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
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime modifiedAt;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String modifiedBy;
    private boolean isFavourite;
    private boolean isArchive;
    private boolean isTrash;

    public FileMetadata(String fileName, String filePath, String directoryName) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.directoryName = directoryName;
    }
}

