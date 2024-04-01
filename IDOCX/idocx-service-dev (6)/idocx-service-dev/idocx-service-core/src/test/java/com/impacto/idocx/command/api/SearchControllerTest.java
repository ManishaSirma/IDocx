package com.impacto.idocx.command.api;

import com.impacto.idocx.command.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService mockSearchService;

    @Test
    void testGetSearchResults() throws Exception {
        // Setup
        doReturn(new PageImpl<>(List.of("value"))).when(mockSearchService).search("searchOn", "field", "operator",
                "workspace", "filter", "value", 0, 0);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/api/v1/search")
                        .param("searchOn", "searchOn")
                        .param("field", "field")
                        .param("operator", "operator")
                        .param("workspace", "workspace")
                        .param("filter", "filter")
                        .param("value", "value")
                        .param("page", "0")
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

    }

    @Test
    void testGetSearchResults_SearchServiceReturnsNoItems() throws Exception {
        // Setup
        doReturn(new PageImpl<>(Collections.emptyList())).when(mockSearchService).search("searchOn", "field",
                "operator", "workspace", "filter", "value", 0, 0);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/api/v1/search")
                        .param("searchOn", "searchOn")
                        .param("field", "field")
                        .param("operator", "operator")
                        .param("workspace", "workspace")
                        .param("filter", "filter")
                        .param("value", "value")
                        .param("page", "0")
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

    }
}
