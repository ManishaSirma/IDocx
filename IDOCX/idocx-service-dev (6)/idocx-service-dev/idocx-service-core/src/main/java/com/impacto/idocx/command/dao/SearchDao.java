package com.impacto.idocx.command.dao;

import com.impacto.idocx.command.entity.FileMetadata;
import com.impacto.idocx.command.entity.FolderMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Log4j2
public class SearchDao {
    private final MongoTemplate mongoTemplate;
    private static final Map<String, Set<String>> validFieldsPerSearchType = new HashMap<>();

    static {
        Set<String> folderFields = new HashSet<>(Arrays.asList("id", "folderName", "folderPath", "workSpaceType"));
        validFieldsPerSearchType.put("FOLDER", folderFields);
        Set<String> documentFields = new HashSet<>(Arrays.asList("id", "fileName", "extension", "tag", "workSpaceType"));
        validFieldsPerSearchType.put("DOCUMENT", documentFields);
    }

    public Page<?> search(String searchOn, String field, String operator, String workspace, String filter, String value, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Criteria> criteriaList = new ArrayList<>();
        if (isValidFieldForSearchType(field, searchOn)) {
            log.info("isValid");
            getFilteredSearch(field, filter, value, criteriaList);
        }

        if (!"BOTH".equals(workspace)) {
            criteriaList.add(Criteria.where("workSpaceType").is(workspace.toUpperCase()));
        }

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        }

        log.info(searchOn);

        if ("FOLDER".equals(searchOn)) {
            return searchInCollection(criteria, pageRequest, FolderMetadata.class);
        } else {
            return searchInCollection(criteria, pageRequest, FileMetadata.class);
        }
    }

    <T> Page<T> searchInCollection(Criteria criteria, PageRequest pageRequest, Class<T> entityClass) {
        Query query = Query.query(criteria).with(pageRequest);
        log.info("Executing query: {}", query.toString());

        List<T> results = mongoTemplate.find(query, entityClass);
        long total = mongoTemplate.count(Query.query(criteria), entityClass);
        return new PageImpl<>(results, pageRequest, total);
    }

    private static boolean isValidFieldForSearchType(String field, String searchType) {
        return validFieldsPerSearchType.getOrDefault(searchType, Set.of()).contains(field);
    }

    private static void getFilteredSearch(String field, String filter, String value, List<Criteria> criteriaList) {
        if (value != null && !value.isEmpty()) {
            switch (filter) {
                case "isEqualTo":
                case "contains":
                    criteriaList.add(Criteria.where(field).regex(".*" + value + ".*", "i")); // 'i' for case-insensitive
                    break;
                case "beginsWith":
                    criteriaList.add(Criteria.where(field).regex("^" + value, "i"));
                    break;
                case "endsWith":
                    criteriaList.add(Criteria.where(field).regex(value + "$", "i"));
                    break;
                case "notContains":
                    criteriaList.add(Criteria.where(field).not().regex(".*" + value + ".*", "i"));
                    break;
                default:
                    break;
            }
        }
    }
}