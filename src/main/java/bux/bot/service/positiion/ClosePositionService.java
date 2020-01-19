package bux.bot.service.positiion;

import bux.bot.model.position.ClosedPosition;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

public class ClosePositionService extends ResilienceService<ClosedPosition> {
    private static final String SERVICE_RESOURCE = "/core/21/users/me/portfolio/positions/%s";
    private static final String SERVICE_HOST = "http://localhost:8080";
    // TODO Encrypt auth keys, e.g. aws kms
    private static final String AUTH_HEADER = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyZWZyZXNoYWJsZSI6ZmFsc2UsInN1YiI6ImJiMGNkYTJiLWExMGUtNGVkMy1hZDVhLTBmODJiNGMxNTJjNCIsImF1ZCI6ImJldGEuZ2V0YnV4LmNvbSIsInNjcCI6WyJhcHA6bG9naW4iLCJydGY6bG9naW4iXSwiZXhwIjoxODIwODQ5Mjc5LCJpYXQiOjE1MDU0ODkyNzksImp0aSI6ImI3MzlmYjgwLTM1NzUtNGIwMS04NzUxLTMzZDFhNGRjOGY5MiIsImNpZCI6Ijg0NzM2MjI5MzkifQ.M5oANIi2nBtSfIfhyUMqJnex-JYg6Sm92KPYaUL9GKg";

    private RestTemplate restTemplate;

    public ClosePositionService() {
        restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(
                new HeaderRequestInterceptor("Authorization", AUTH_HEADER)
        ));
    }

    public ClosedPosition closePosition(String productId) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(SERVICE_HOST)
                .path(String.format(SERVICE_RESOURCE, productId))
                .build()
                .encode()
                .toUri();

        return call(uri);
    }

    @Override
    protected ClosedPosition getFutureSupplier(URI uri) {
        return restTemplate
                .exchange(uri, HttpMethod.DELETE, null, ClosedPosition.class)
                .getBody();
    }
}
