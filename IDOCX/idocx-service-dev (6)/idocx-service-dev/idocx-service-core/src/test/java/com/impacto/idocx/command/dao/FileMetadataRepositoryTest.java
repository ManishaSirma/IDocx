package com.impacto.idocx.command.dao;

import com.impacto.idocx.command.entity.FileMetadata;
import com.impacto.idocx.command.service.WorkSpaceStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileMetadataRepositoryTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    private WorkSpaceStorageService fileMetadataService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

    }

//    @Test
//    void testFindByFileNameAndDirectoryName_SuccessfulRetrieval() {
//
//        String fileName = "example.txt";
//        String directoryName = "/path/to/directory";
//        FileMetadata expectedFileMetadata = new FileMetadata();
//        expectedFileMetadata.setFileName(fileName);
//        expectedFileMetadata.setDirectoryName(directoryName);
//        fileMetadataRepository.save(expectedFileMetadata);
//
//        when(fileMetadataRepository.findByFileNameAndDirectoryName(directoryName, fileName))
//                .thenReturn(Optional.of(expectedFileMetadata));
//
//
//        Optional<FileMetadata> actualFileMetadata = fileMetadataRepository.findByFileNameAndDirectoryName(directoryName, fileName);
//
//
//        assertTrue(actualFileMetadata.isPresent());
//        assertEquals(expectedFileMetadata, actualFileMetadata.get());
//    }

//    @Test
//    void testFindByFileNameAndDirectoryName_NoMatch() {
//
//        String fileName = "example.txt";
//        String directoryName = "/path/to/directory";
//
//        when(fileMetadataRepository.findByFileNameAndDirectoryName(directoryName, fileName))
//                .thenReturn(Optional.empty());
//
//        Optional<FileMetadata> actualFileMetadata = fileMetadataRepository.findByFileNameAndDirectoryName(directoryName, fileName);
//
//
//        assertFalse(actualFileMetadata.isPresent());
//    }

}
