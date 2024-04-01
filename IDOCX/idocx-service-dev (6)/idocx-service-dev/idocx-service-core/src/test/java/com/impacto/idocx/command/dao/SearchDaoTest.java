package com.impacto.idocx.command.dao;

import com.impacto.idocx.command.entity.FileMetadata;
import com.impacto.idocx.command.entity.FolderMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchDaoTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private SearchDao searchDao;

    @Test
    void testSearchInFoldersByValidField() {
        FolderMetadata folderMetadata = new FolderMetadata("1", "Test Folder", "/test/path", null, null, false, false, false);
        when(mongoTemplate.find(any(), eq(FolderMetadata.class))).thenReturn(List.of(folderMetadata));
        when(mongoTemplate.count(any(), eq(FolderMetadata.class))).thenReturn(1L);

        Page<?> result = searchDao.search("FOLDER", "folderName", "contains", "BOTH", "Test", "Test Folder", 0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(mongoTemplate, times(1)).find(any(), eq(FolderMetadata.class));
    }

    @Test
    void searchInFileCollection_ReturnsPageOfFileMetadata() {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId("1");
        fileMetadata.setFileName("Test File");
        when(mongoTemplate.find(any(), eq(FileMetadata.class))).thenReturn(List.of(fileMetadata));
        when(mongoTemplate.count(any(), eq(FileMetadata.class))).thenReturn(1L);

        Page<?> result = searchDao.search("DOCUMENT", "fileName", "contains", "BOTH", "Test", "Test File", 0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(mongoTemplate, times(1)).find(any(), eq(FileMetadata.class));
    }

    @Test
    void testSearchDocumentsWithContainsFilter() {
        FileMetadata fileMetadata = new FileMetadata("1", "Test Document", "/test/path/document", "TestDir", "DOC123", "pdf", false, "Tag1", 1, "Remarks", "Authorizer", null, null, null, null, null, false, false, false);
        when(mongoTemplate.find(any(), eq(FileMetadata.class))).thenReturn(List.of(fileMetadata));
        when(mongoTemplate.count(any(), eq(FileMetadata.class))).thenReturn(1L);

        Page<?> resultContains = searchDao.search("DOCUMENT", "fileName", "contains", "BOTH", "Test", "Test Document", 0, 10);

        assertNotNull(resultContains);
        assertFalse(resultContains.isEmpty());
        assertEquals(1, resultContains.getTotalElements());
        verify(mongoTemplate, times(1)).find(any(), eq(FileMetadata.class));
    }

}
