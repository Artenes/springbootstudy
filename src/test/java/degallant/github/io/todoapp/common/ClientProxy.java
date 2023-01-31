package degallant.github.io.todoapp.common;

import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClientConfigurer;

public class ClientProxy {

    private WebTestClient client;

    public ClientProxy(WebTestClient client) {
        this.client = client;
    }

    public WebTestClient.RequestBodyUriSpec post() {
        return client.post();
    }

    public void mutateWith(WebTestClientConfigurer configure) {
        client = client.mutateWith(configure);
    }

}
