package com.example.exception;

/**
 * 权限不足（403）。
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String msg) {
        super(msg);
    }
}
