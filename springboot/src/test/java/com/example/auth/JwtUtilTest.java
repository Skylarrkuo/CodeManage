package com.example.auth;

import com.example.auth.support.JwtTestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    /** 每个用例前都重置静态状态，避免上一个用例改了密钥/有效期后污染下一个 */
    @BeforeEach
    void resetJwtState() {
        JwtTestSupport.init();
    }

    @AfterAll
    static void restoreJwtState() {
        JwtTestSupport.init();
    }

    private static LoginUser sampleUser() {
        return new LoginUser(7, "stu7", "学生七", "STUDENT");
    }

    @Test
    void createAndParseRoundTrip() {
        String token = JwtUtil.createToken(sampleUser());
        assertThat(token).isNotBlank();

        LoginUser parsed = JwtUtil.parse(token);
        assertThat(parsed).isNotNull();
        assertThat(parsed.getId()).isEqualTo(7);
        assertThat(parsed.getUsername()).isEqualTo("stu7");
        assertThat(parsed.getName()).isEqualTo("学生七");
        assertThat(parsed.getRole()).isEqualTo("STUDENT");
    }

    @Test
    void parseReturnsNullForExpiredToken() {
        // 有效期为负 → 签发即过期
        JwtTestSupport.init(JwtTestSupport.SECRET, -1);
        String expired = JwtUtil.createToken(sampleUser());

        assertThat(JwtUtil.parse(expired)).isNull();
    }

    @Test
    void parseReturnsNullForTokenSignedWithAnotherSecret() {
        JwtTestSupport.init("another-secret-key-0123456789abcdef", 12);
        String foreignToken = JwtUtil.createToken(sampleUser());

        // 换回测试密钥后再解析，签名不匹配
        JwtTestSupport.init(JwtTestSupport.SECRET, 12);
        assertThat(JwtUtil.parse(foreignToken)).isNull();
    }

    @Test
    void parseReturnsNullForTamperedToken() {
        String token = JwtUtil.createToken(sampleUser());
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(JwtUtil.parse(tampered)).isNull();
    }

    @Test
    void parseReturnsNullForGarbageToken() {
        assertThat(JwtUtil.parse("abc")).isNull();
        assertThat(JwtUtil.parse("a.b.c")).isNull();
        assertThat(JwtUtil.parse("Bearer something")).isNull();
    }

    @Test
    void parseReturnsNullForBlankToken() {
        assertThat(JwtUtil.parse(null)).isNull();
        assertThat(JwtUtil.parse("")).isNull();
        assertThat(JwtUtil.parse("   ")).isNull();
    }
}
