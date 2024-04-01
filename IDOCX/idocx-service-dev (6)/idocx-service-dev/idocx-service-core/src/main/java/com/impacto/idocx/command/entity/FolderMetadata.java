package com.impacto.idocx.command.entity;

import com.impacto.idocx.command.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "folderMetadata")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderMetadata {
    @Id
    private String id;
    private String folderName;
    private String folderPath;
    private Constants.WORKSPACE_TYPE workSpaceType;
    private List<FileMetadata> documents;
    private boolean isFavourite;
    private boolean isArchive;
    private boolean isTrash;

}
