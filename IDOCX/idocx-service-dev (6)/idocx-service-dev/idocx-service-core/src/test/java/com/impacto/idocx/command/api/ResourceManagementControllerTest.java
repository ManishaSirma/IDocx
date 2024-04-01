package com.impacto.idocx.command.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.impacto.idocx.command.common.Constants;
import com.impacto.idocx.command.common.GenericResponse;
import com.impacto.idocx.command.dtos.FileMetaDataDto;
import com.impacto.idocx.command.dtos.IdsRequestDto;
import com.impacto.idocx.command.model.ResourceManagementRequest;
import com.impacto.idocx.command.service.ResourceManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ResourceManagementController.class)
class ResourceManagementControllerTest {
    public static final String FILE_NAME = "FileName";
    public static final String SUCCESS = "success";
    public static final String PATH = "/v1/update";
    public static final String PATH1 = "/v1/list";
    public static final String ACTION = "action";
    public static final String FAVOURITE = "favourite";
    public static final String DOCUMENT = "document";
    private static final String DIRECTORY_PATH = "ABC/DEF/IJK";
    public static final String TYPE = "resourceType";
    public static final String FOLDER = "folder";
    public static final int STATUSCODE200 = 200;
    @Autowired
    private ObjectMapper mockObjectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ResourceManagementService mockResourceManagementService;

    @Test
    void testUpdateResourceStatus() throws Exception {
        ResourceManagementRequest uploadRequest = new ResourceManagementRequest(
                FAVOURITE,
                DOCUMENT,
                Arrays.asList("1", "2"),
                true
        );
        final GenericResponse<?> expectedResponse = new GenericResponse<>(STATUSCODE200, SUCCESS, createFileMetaDataDtoTest(FILE_NAME));
        doReturn(expectedResponse).when(mockResourceManagementService).updateResourceStatus(uploadRequest);

        final MockHttpServletResponse actualResponse = mockMvc.perform(put(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mockObjectMapper.writeValueAsString(uploadRequest)))
                .andReturn()
                .getResponse();


        assertThat(actualResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(actualResponse.getContentAsString()).isEqualTo(mockObjectMapper.writeValueAsString(expectedResponse));
    }

    private FileMetaDataDto createFileMetaDataDtoTest(String documentName) {
        FileMetaDataDto fileMetaDataDto = new FileMetaDataDto();
        fileMetaDataDto.setFileName(documentName);
        fileMetaDataDto.setFilePath(DIRECTORY_PATH + "/" + documentName);
        fileMetaDataDto.setDirectoryName(DIRECTORY_PATH);
        fileMetaDataDto.setWorkSpaceType(Constants.WORKSPACE_TYPE.AUTOWORKSPACE);
        return fileMetaDataDto;
    }

    @Test
    void deleteFilesOrFoldersSuccess() throws Exception {
        IdsRequestDto idsRequestDto = new IdsRequestDto();
        idsRequestDto.setType("FILE");
        idsRequestDto.setIds(Arrays.asList("file1", "file2"));

        doNothing().when(mockResourceManagementService).deleteResource(idsRequestDto);

        mockMvc.perform(delete("/v1/delete-resource")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mockObjectMapper.writeValueAsString(idsRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(Constants.RESPONSE_STATUS.OK.getValue()))
                .andExpect(jsonPath("$.message").value(Constants.RESPONSE_MESSAGE.SUCCESS.getValue()))
                .andExpect(jsonPath("$.data").value("Successfully deleted the :FILE"));

        verify(mockResourceManagementService, times(1)).deleteResource(idsRequestDto);
    }

    @Test
    void testGetResources() throws Exception {
        Page<FileMetaDataDto> mockPage = new PageImpl<>(Collections.singletonList(createFileMetaDataDtoTest("testDoc")));
        doReturn(mockPage).when(mockResourceManagementService).getResources(anyString(), anyString(), anyInt(), anyInt());
        mockMvc.perform(get("/v1/list")
                        .param("action", "trash")
                        .param("resourceType", "document")
                        .param("pageSize", "10")
                        .param("pageNo", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // Here you would add more detailed expectations regarding the JSON structure
                // based on the specifics of your GenericResponse and the data it contains.
                .andExpect(content().json("{\"status\":200,\"message\":\"success\",\"data\":[{\"fileName\": \"testDoc\"}],\"total\":1,\"count\":1,\"currentPage\":0}", false));
    }
    private Page<FileMetaDataDto> createMockFileMetaDataPage() {
        List<FileMetaDataDto> list = List.of(new FileMetaDataDto()); // Assume FileMetaDataDto has a no-arg constructor or provide necessary arguments
        return new PageImpl<>(list);
    }
}