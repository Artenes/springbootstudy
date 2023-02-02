package degallant.github.io.todoapp.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/** @noinspection unused*/
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "36000")
public abstract class IntegrationTest {

    protected static final String DEFAULT_USER = "default@gmail.com";

    @Autowired
    private WebTestClient client;

    @Autowired
    private Flyway flyway;

    @MockBean
    private OpenIdTokenParser openIdTokenParser;

    @Autowired
    private ObjectMapper mapper;

    protected Request request;

    protected EntityRequest entityRequest;

    protected Authenticator authenticator;

    @BeforeEach
    public void setUp() {
        ClientProxy proxy = new ClientProxy(client);
        authenticator = new Authenticator(proxy, openIdTokenParser, mapper);
        request = new Request(proxy, authenticator, mapper);
        entityRequest = new EntityRequest(request);
        flyway.migrate();
    }

    @AfterEach
    public void tearDown() {
        flyway.clean();
    }

}