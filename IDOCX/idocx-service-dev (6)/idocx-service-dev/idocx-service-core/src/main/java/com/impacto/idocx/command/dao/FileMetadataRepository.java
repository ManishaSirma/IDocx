package com.impacto.idocx.command.dao;

import com.impacto.idocx.command.entity.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {
    Optional<FileMetadata> findByFileNameAndFilePath(String fileName, String filePath);

    Page<FileMetadata> findAllByFilePath(String filePath, PageRequest pageRequest);

    Page<FileMetadata> findAllByFilePathAndIsTrashFalseAndIsArchiveFalse(String filePath, PageRequest pageRequest);

    List<FileMetadata> findAllByWorkSpaceType(String workspaceType);

    Optional<FileMetadata> findByFileName(String fileName);

    Page<FileMetadata> findByIsFavouriteTrue(PageRequest page);

    Page<FileMetadata> findByIsArchiveTrue(PageRequest page);

    Page<FileMetadata> findByIsTrashTrue(PageRequest page);
}

