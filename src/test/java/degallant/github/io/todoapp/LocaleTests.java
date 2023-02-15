package degallant.github.io.todoapp;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class LocaleTests extends IntegrationTest {

    @Test
    public void test() {

        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "")
                .post().isBadRequest()
                .hasField("$.errors[0].message", contains("No value provided"));

        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "")
                .withHeader(HttpHeaders.ACCEPT_LANGUAGE, "pt-br")
                .post().isBadRequest()
                .hasField("$.errors[0].message", contains("Nenhum valor informado"));

    }

}
