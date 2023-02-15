package degallant.github.io.todoapp;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class HeaderContentTests extends IntegrationTest {

    @Test
    public void locale_failsWhenLocaleIsEmpty() {

        request.asUser(DEFAULT_USER).to("tasks")
                .withHeader(HttpHeaders.ACCEPT_LANGUAGE, "")
                .get().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.not_a_locale"));

    }

    @Test
    public void locale_failsWhenLocaleIsInvalid() {

        request.asUser(DEFAULT_USER).to("tasks")
                .withHeader(HttpHeaders.ACCEPT_LANGUAGE, "invalid")
                .get().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.not_a_locale"));

    }

    @Test
    public void locale_failsWhenLocaleIsNotSupported() {

        request.asUser(DEFAULT_USER).to("tasks")
                .withHeader(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR")
                .get().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.not_a_locale"));

    }

    @Test
    public void locale_showsTranslatedMessageWhenLocaleIsChanged() {

        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "")
                .post().isBadRequest()
                .hasField("$.errors[0].message", contains("No value provided"));

        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "")
                .withHeader(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR")
                .post().isBadRequest()
                .hasField("$.errors[0].message", contains("Nenhum valor informado"));

    }

    @Test
    public void offset_failsWhenOffsetIsEmpty() {

        request.asUser(DEFAULT_USER).to("tasks")
                .withHeader("Accept-Offset", "")
                .get().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.not_an_offset"));

    }

    @Test
    public void offset_failsWhenOffsetIsInvalid() {

        request.asUser(DEFAULT_USER).to("tasks")
                .withHeader("Accept-Offset", "invalid")
                .get().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.not_an_offset"));

    }

    @Test
    public void offset_failsWhenOffsetIsOutOfRange() {

        request.asUser(DEFAULT_USER).to("tasks")
                .withHeader("Accept-Offset", "+34:00")
                .get().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.not_an_offset"));

    }

}
