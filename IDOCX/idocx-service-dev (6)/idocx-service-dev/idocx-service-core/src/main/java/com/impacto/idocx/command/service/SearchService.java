package com.impacto.idocx.command.service;

import com.impacto.idocx.command.dao.SearchDao;
import com.impacto.idocx.command.entity.FileMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SearchDao searchDao;

    public Page<?> search(String searchOn, String field, String operator, String workspace, String filter, String value, int page, int size) {
        return searchDao.search(searchOn,field, operator, workspace, filter, value, page, size);

    }
}
