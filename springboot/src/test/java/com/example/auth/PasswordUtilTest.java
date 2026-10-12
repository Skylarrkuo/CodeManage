package com.example.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordUtilTest {

    @Test
    void encodeProducesBcryptHash() {
        String hash = PasswordUtil.encode("123456");
        assertThat(hash).startsWith("$2");
        assertThat(hash).hasSize(60);
    }

    @Test
    void encodeUsesRandomSaltSoTwoHashesDiffer() {
        assertThat(PasswordUtil.encode("123456")).isNotEqualTo(PasswordUtil.encode("123456"));
    }

    @Test
    void matchesAcceptsCorrectPassword() {
        String hash = PasswordUtil.encode("123456");
        assertThat(PasswordUtil.matches("123456", hash)).isTrue();
    }

    @Test
    void matchesRejectsWrongPassword() {
        String hash = PasswordUtil.encode("123456");
        assertThat(PasswordUtil.matches("wrong", hash)).isFalse();
        assertThat(PasswordUtil.matches("1234567", hash)).isFalse();
    }

    @Test
    void matchesRejectsPlaintextStoredValue() {
        // 明文兼容分支已移除：库中若仍是明文，必须登录失败，而不是静默放行
        assertThat(PasswordUtil.matches("123456", "123456")).isFalse();
        assertThat(PasswordUtil.matches("admin", "admin")).isFalse();
    }

    @Test
    void matchesRejectsBlankOrNullInputs() {
        String hash = PasswordUtil.encode("123456");
        assertThat(PasswordUtil.matches(null, hash)).isFalse();
        assertThat(PasswordUtil.matches("", hash)).isFalse();
        assertThat(PasswordUtil.matches("123456", null)).isFalse();
        assertThat(PasswordUtil.matches("123456", "")).isFalse();
    }

    @Test
    void matchesRejectsMalformedHash() {
        assertThat(PasswordUtil.matches("123456", "$2a$10$not-a-valid-hash")).isFalse();
    }
}
