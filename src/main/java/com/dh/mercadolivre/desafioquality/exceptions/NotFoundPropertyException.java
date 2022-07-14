package com.dh.mercadolivre.desafioquality.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundPropertyException extends RuntimeException {

    public NotFoundPropertyException(String message) {
        super(message);
    }
}
