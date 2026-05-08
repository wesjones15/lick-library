package org.jones.licklibrary.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LickNotFoundException extends RuntimeException {
    public LickNotFoundException(UUID id) {
        super("Lick not found: " + id);
    }
}
