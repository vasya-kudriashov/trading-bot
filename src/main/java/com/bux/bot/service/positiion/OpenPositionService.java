package com.bux.bot.service.positiion;

import com.bux.bot.model.position.OpenedPosition;
import com.bux.bot.model.trade.Trade;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;

public class OpenPositionService extends Resilience4jService<OpenedPosition> {
    // TODO Store in .properties in order to separate configuration from code
    private static final String SERVICE_RESOURCE = "/core/21/users/me/trades";
    private static final String SERVICE_HOST = "https://api.beta.getbux.com";
    // TODO Encrypt auth keys, e.g. aws kms
    private static final String AUTH_HEADER = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyZWZyZXNoYWJsZSI6ZmFsc2UsInN1YiI6ImJiMGNkYTJiLWExMGUtNGVkMy1hZDVhLTBmODJiNGMxNTJjNCIsImF1ZCI6ImJldGEuZ2V0YnV4LmNvbSIsInNjcCI6WyJhcHA6bG9naW4iLCJydGY6bG9naW4iXSwiZXhwIjoxODIwODQ5Mjc5LCJpYXQiOjE1MDU0ODkyNzksImp0aSI6ImI3MzlmYjgwLTM1NzUtNGIwMS04NzUxLTMzZDFhNGRjOGY5MiIsImNpZCI6Ijg0NzM2MjI5MzkifQ.M5oANIi2nBtSfIfhyUMqJnex-JYg6Sm92KPYaUL9GKg";
    private static final String ACCEPT_LANGUAGE_HEADER = "nl-NL,en;q=0.8";
    private static RestTemplate restTemplate = new RestTemplate();
    private Trade trade = Trade.builder().build();

    public OpenPositionService() {
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.setInterceptors(Arrays.asList(
                new HeaderRequestInterceptor("Authorization", AUTH_HEADER),
                new HeaderRequestInterceptor("Accept-Language",  ACCEPT_LANGUAGE_HEADER)
        ));
    }

    public OpenedPosition openPosition(Trade trade) {
        this.trade = trade;

        URI uri = UriComponentsBuilder
                .fromHttpUrl(SERVICE_HOST)
                .path(SERVICE_RESOURCE)
                .build()
                .encode()
                .toUri();

        return call(uri);
    }

    @Override
    protected OpenedPosition getFutureSupplier(URI uri) {
        return restTemplate.postForObject(uri, trade , OpenedPosition.class);
    }
}
