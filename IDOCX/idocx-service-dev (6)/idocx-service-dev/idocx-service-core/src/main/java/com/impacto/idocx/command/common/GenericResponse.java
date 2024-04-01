package com.impacto.idocx.command.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GenericResponse<T> {
    int status;
    String message;
    T data;
    long total;
    long count;
    int currentPage;

    public GenericResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
