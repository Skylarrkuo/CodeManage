package com.example.exception;

/**
 * 未认证 / 登录态失效（401）。
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String msg) {
        super(msg);
    }
}
