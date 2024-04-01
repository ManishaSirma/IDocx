package com.impacto.idocx.command.api;

import com.impacto.idocx.command.common.Constants;
import com.impacto.idocx.command.common.GenericResponse;
import com.impacto.idocx.command.dtos.IdsRequestDto;
import com.impacto.idocx.command.model.ResourceManagementRequest;
import com.impacto.idocx.command.service.ResourceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class ResourceManagementController {

    private final ResourceManagementService resourceManagementService;

    @Operation(summary = "Update Resource Status",
            description = "Updates the status of a resource specified by the given request.")
    @PutMapping("/update")
    public ResponseEntity<GenericResponse<?>> updateResourceStatus(@RequestBody ResourceManagementRequest uploadRequest) {
        GenericResponse<?> response = resourceManagementService.updateResourceStatus(uploadRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Get Resources",
            description = "Get a list of resources based on action, type, page size, and page number.")
    @GetMapping("/list")
    public ResponseEntity<GenericResponse<List<?>>> getResources(@RequestParam String action,
                                                                 @RequestParam String resourceType,
                                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                                 @RequestParam(defaultValue = "0") int pageNo
    ) {
        Page<?> metaDataList = resourceManagementService.getResources(action, resourceType, pageNo, pageSize);
        GenericResponse<List<?>> response = new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                metaDataList.getContent(),
                metaDataList.getTotalElements(),
                metaDataList.getNumberOfElements(),
                metaDataList.getNumber()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Delete Files or Folders", description = "Delete files or folders based on the provided IDs.")
    @DeleteMapping("/delete-resource")
    public ResponseEntity<GenericResponse<String>> deleteFilesOrFolders(@RequestBody IdsRequestDto idsRequestDto) {
        this.resourceManagementService.deleteResource(idsRequestDto);
        GenericResponse<String> genericResponse = new GenericResponse<>(
                Constants.RESPONSE_STATUS.OK.getValue(),
                Constants.RESPONSE_MESSAGE.SUCCESS.getValue(),
                "Successfully deleted the :" + idsRequestDto.getType()
        );
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }

}
