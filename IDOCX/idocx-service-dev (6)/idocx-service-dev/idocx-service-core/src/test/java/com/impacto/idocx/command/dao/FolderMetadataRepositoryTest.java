package com.impacto.idocx.command.dao;

import com.impacto.idocx.command.entity.FolderMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FolderMetadataRepositoryTest {

    @Mock
    private FolderMetadataRepository folderMetadataRepository;

    @Test
    public void testGetFoldersByWorkspaceType() {
        String workspaceType = "AUTOWORKSPACE";
        List<FolderMetadata> expectedFolders = Arrays.asList(new FolderMetadata(), new FolderMetadata());
        when(folderMetadataRepository.findAllByWorkSpaceType(workspaceType)).thenReturn(expectedFolders);

        List<FolderMetadata> resultFolders = folderMetadataRepository.findAllByWorkSpaceType(workspaceType);

        assertEquals(expectedFolders, resultFolders, "The returned folders should match the expected ones.");
    }


    @Test
    public void testGetFoldersByInvalidWorkspaceType() {
        String invalidWorkspaceType = "INVALID_WORKSPACE_TYPE";
        when(folderMetadataRepository.findAllByWorkSpaceType(anyString())).thenReturn(Arrays.asList());

        List<FolderMetadata> resultFolders = folderMetadataRepository.findAllByWorkSpaceType(invalidWorkspaceType);

        assertEquals(0, resultFolders.size(), "No folders should be returned for an invalid workspace type.");
    }

    @Test
    public void testGetFoldersByNullWorkspaceType() {
        String nullWorkspaceType = null;
        when(folderMetadataRepository.findAllByWorkSpaceType(null)).thenReturn(Arrays.asList());

        List<FolderMetadata> resultFolders = folderMetadataRepository.findAllByWorkSpaceType(nullWorkspaceType);

        assertEquals(0, resultFolders.size(), "No folders should be returned for a null workspace type.");
    }

    @Test
    public void testGetFolderByValidPath() {
        String folderPath = "/valid/path";
        FolderMetadata expectedFolder = new FolderMetadata();
        when(folderMetadataRepository.findByFolderPath(folderPath)).thenReturn(Optional.of(expectedFolder));

        Optional<FolderMetadata> resultFolder = folderMetadataRepository.findByFolderPath(folderPath);

        assertEquals(expectedFolder, resultFolder.orElse(null), "The returned folder should match the expected one.");
    }

    @Test
    public void testGetFolderByInvalidPath() {
        String invalidPath = "/invalid/path";
        when(folderMetadataRepository.findByFolderPath(invalidPath)).thenReturn(Optional.empty());

        Optional<FolderMetadata> resultFolder = folderMetadataRepository.findByFolderPath(invalidPath);

        assertEquals(Optional.empty(), resultFolder, "No folder should be returned for an invalid path.");
    }
}
