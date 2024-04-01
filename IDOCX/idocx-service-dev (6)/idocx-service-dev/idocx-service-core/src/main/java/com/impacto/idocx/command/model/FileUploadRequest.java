package com.impacto.idocx.command.model;

import com.impacto.idocx.command.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FileUploadRequest {
    private String filePath;
    private List<MultipartFile> files;
    private String fileName;
    private String documentId;
    private boolean passwordProtected;
    private String tag;
    private int version;
    private String remarks;
    private String workspaceType;
    private String folderName;


}
