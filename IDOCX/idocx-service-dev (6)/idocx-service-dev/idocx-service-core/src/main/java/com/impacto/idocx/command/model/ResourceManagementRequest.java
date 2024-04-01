package com.impacto.idocx.command.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceManagementRequest {
    private String action;
    private String resourceType;
    private List<String> ids;
    private boolean status;

}