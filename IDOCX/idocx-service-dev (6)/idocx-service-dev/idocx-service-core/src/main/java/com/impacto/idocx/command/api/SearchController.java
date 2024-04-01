package com.impacto.idocx.command.api;

import com.impacto.idocx.command.common.Constants;
import com.impacto.idocx.command.common.GenericResponse;
import com.impacto.idocx.command.entity.FileMetadata;
import com.impacto.idocx.command.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {
    private final SearchService searchService;


    @Operation(summary = "Get Search Results",
            description = "Retrieve a list of search results based on provided parameters.")
    @GetMapping
    public ResponseEntity<GenericResponse<List<?>>> getSearchResults(@RequestParam(required = false) String searchOn,
                                                                     @RequestParam(required = false) String field,
                                                                     @RequestParam(required = false) String operator,
                                                                     @RequestParam(required = false) String workspace,
                                                                     @RequestParam(required = false) String filter,
                                                                     @RequestParam(required = false) String value,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size) {
        Page<?> dataList = searchService.search(searchOn, field, operator, workspace, filter, value, page, size);
        GenericResponse<List<?>> response = new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                dataList.getContent(),
                dataList.getTotalElements(),
                dataList.getNumberOfElements(),
                dataList.getNumber()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
