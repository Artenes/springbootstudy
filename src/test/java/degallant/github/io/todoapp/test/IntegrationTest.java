package degallant.github.io.todoapp.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.authentication.JwtToken;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import degallant.github.io.todoapp.domain.users.Role;
import degallant.github.io.todoapp.domain.users.UsersRepository;
import net.minidev.json.JSONArray;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.JsonPathAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @noinspection unused, SameParameterValue
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "36000")
public abstract class IntegrationTest {

    protected static final String DEFAULT_USER = "default@gmail.com";
    protected static final String ANOTHER_USER = "another@gmail.com";
    protected static final String ADMIN_USER = "admin@gmail.com";

    @Autowired
    private WebTestClient client;

    @Autowired
    private Flyway flyway;

    @MockBean
    private OpenIdTokenParser openIdTokenParser;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    protected JwtToken token;

    @Autowired
    private UsersRepository usersRepository;

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
        makeAdmin();
    }

    private void makeAdmin() {
        var id = authenticator.makeUser(ADMIN_USER);
        var entity = usersRepository.findById(id).orElseThrow();
        entity.setRole(Role.ROLE_ADMIN);
        usersRepository.save(entity);
    }

    @AfterEach
    public void tearDown() {
        flyway.clean();
    }

    protected Consumer<JsonPathAssertions> isEqualTo(Object value) {
        return v -> v.isEqualTo(value.toString());
    }

    protected Consumer<JsonPathAssertions> exists() {
        return JsonPathAssertions::hasJsonPath;
    }

    protected Consumer<JsonPathAssertions> isEmpty() {
        return JsonPathAssertions::isEmpty;
    }

    protected Consumer<JsonPathAssertions> doesNotExists() {
        return JsonPathAssertions::doesNotExist;
    }

    protected Consumer<JsonPathAssertions> contains(String value) {
        return v -> v.value(containsString(value));
    }

    /**
     * Retardedly, but understandable, the JSON Path implementation we are using does not allow to do this:
     * <p>
     * $.errors[?(@.field == 'user_id')].type.first()
     * <p>
     * Where after applying a filter, we are able to get an element from the list of results.
     * <p>
     * So to bypass this we just need to peek the result array and compare the value ourselves.
     * <p>
     * This was never implemented (since 2016) because "is not supported on any of the implementations".
     * https://github.com/json-path/JsonPath/issues/272
     *
     * @param value the value to verify against the first item found
     * @return the consumer that will validate the result
     */
    protected Consumer<JsonPathAssertions> firstContains(String value) {
        return v -> {
            v.value(consumed -> {
                var list = (JSONArray) consumed;
                if (list.isEmpty()) {
                    fail("No result found with provided jsonpath");
                }
                if (!list.get(0).toString().contains(value)) {
                    fail("First item (" + list.get(0) + ") does not contains " + value);
                }
            });
        };
    }

}
