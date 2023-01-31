package degallant.github.io.todoapp.common;

import com.fasterxml.jackson.databind.ObjectMapper;

/** @noinspection ClassCanBeRecord*/
public class Request {

    private final ClientProxy client;
    private final ObjectMapper mapper;
    private final Authenticator authenticator;

    public Request(ClientProxy client, Authenticator authenticator, ObjectMapper mapper) {
        this.client = client;
        this.authenticator = authenticator;
        this.mapper = mapper;
    }

    public AuthenticatedRequest asUser(String email) {
        return new AuthenticatedRequest(mapper, authenticator, email, client);
    }

    public AuthenticatedRequest asGuest() {
        return new AuthenticatedRequest(mapper, authenticator, null, client);
    }


}
