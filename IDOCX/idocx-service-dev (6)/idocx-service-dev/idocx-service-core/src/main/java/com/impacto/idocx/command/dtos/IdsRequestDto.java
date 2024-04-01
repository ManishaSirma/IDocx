package com.impacto.idocx.command.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdsRequestDto {
    private List<String> ids;
    private String type;

}
