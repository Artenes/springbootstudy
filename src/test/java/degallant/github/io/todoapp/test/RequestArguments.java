package degallant.github.io.todoapp.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

class RequestArguments {

    private final ClientProxy client;
    private final ObjectMapper mapper;
    private final Authenticator authenticator;
    private final Map<String, Object> body = new HashMap<>();
    private final Map<String, Object> params = new HashMap<>();
    private final Map<String, Object> headers = new HashMap<>();
    private Request.AuthInfo authInfo;
    private String version;
    private URI uri;
    private String path;
    private Object rawBody;
    private WebTestClient.ResponseSpec responseSpec;
    private WebTestClient.BodyContentSpec bodySpec;
    private EntityExchangeResult<byte[]> response;

    public RequestArguments(ClientProxy client, ObjectMapper mapper, Authenticator authenticator) {
        this.client = client;
        this.mapper = mapper;
        this.authenticator = authenticator;
    }

    public ClientProxy getClient() {
        return client;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public Request.AuthInfo getAuthInfo() {
        return authInfo;
    }

    public void setAuthInfo(Request.AuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = "v" + version;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Object getRawBody() {
        return rawBody;
    }

    public void setRawBody(Object rawBody) {
        this.rawBody = rawBody;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public WebTestClient.ResponseSpec getResponseSpec() {
        return responseSpec;
    }

    public void setResponseSpec(WebTestClient.ResponseSpec responseSpec) {
        this.responseSpec = responseSpec;
    }

    public WebTestClient.BodyContentSpec getBodySpec() {
        return bodySpec;
    }

    public void setBodySpec(WebTestClient.BodyContentSpec bodySpec) {
        this.bodySpec = bodySpec;
    }

    public EntityExchangeResult<byte[]> getResponse() {
        return response;
    }

    public void setResponse(EntityExchangeResult<byte[]> response) {
        this.response = response;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }
}
