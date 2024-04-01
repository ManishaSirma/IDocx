package com.impacto.idocx.command.dao;

import com.impacto.idocx.command.entity.FileMetadata;
import com.impacto.idocx.command.entity.FolderMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderMetadataRepository extends MongoRepository<FolderMetadata, String> {
    List<FolderMetadata> findAllByWorkSpaceType(String workSpaceType);

    List<FolderMetadata> findByWorkSpaceTypeAndIsTrashFalseAndIsArchiveFalse(String workSpaceType);

    Optional<FolderMetadata> findByFolderPath(String folderPath);

    Page<FolderMetadata> findByIsFavouriteTrue(PageRequest page);

    Page<FolderMetadata> findByIsArchiveTrue(PageRequest page);

    Page<FolderMetadata> findByIsTrashTrue(PageRequest page);

}