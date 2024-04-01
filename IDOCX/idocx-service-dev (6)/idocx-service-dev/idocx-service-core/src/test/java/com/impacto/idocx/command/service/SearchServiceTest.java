package com.impacto.idocx.command.service;

import com.impacto.idocx.command.dao.SearchDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private SearchDao mockSearchDao;

    private SearchService searchServiceUnderTest;

    @BeforeEach
    void setUp() {
        searchServiceUnderTest = new SearchService(mockSearchDao);
    }

    @Test
    void testSearch() {

        doReturn(new PageImpl<>(List.of("value"))).when(mockSearchDao).search("searchOn", "field", "operator",
                "workspace", "filter", "value", 0, 0);

        final Page<?> result = searchServiceUnderTest.search("searchOn", "field", "operator", "workspace", "filter",
                "value", 0, 0);

        verify(mockSearchDao).search(
                eq("searchOn"), eq("field"), eq("operator"), eq("workspace"), eq("filter"), eq("value"), eq(0), eq(0));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        assertEquals("value", result.getContent().get(0));
    }

    @Test
    void testSearch_SearchDaoReturnsNoItems() {

        doReturn(new PageImpl<>(Collections.emptyList())).when(mockSearchDao).search("searchOn", "field", "operator",
                "workspace", "filter", "value", 0, 0);

        final Page<?> result = searchServiceUnderTest.search("searchOn", "field", "operator", "workspace", "filter",
                "value", 0, 0);

        verify(mockSearchDao).search("searchOn", "field", "operator", "workspace", "filter", "value", 0, 0);

    }
}
