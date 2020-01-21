package com.bux.bot.service.pricefeed;

import javax.websocket.ClientEndpointConfig;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FeedServiceConfigurator extends ClientEndpointConfig.Configurator {
    // TODO Encrypt auth keys, e.g. aws kms
    private static final String AUTH_HEADER = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyZWZyZXNoYWJsZSI6ZmFsc2UsInN1YiI6ImJiMGNkYTJiLWExMGUtNGVkMy1hZDVhLTBmODJiNGMxNTJjNCIsImF1ZCI6ImJldGEuZ2V0YnV4LmNvbSIsInNjcCI6WyJhcHA6bG9naW4iLCJydGY6bG9naW4iXSwiZXhwIjoxODIwODQ5Mjc5LCJpYXQiOjE1MDU0ODkyNzksImp0aSI6ImI3MzlmYjgwLTM1NzUtNGIwMS04NzUxLTMzZDFhNGRjOGY5MiIsImNpZCI6Ijg0NzM2MjI5MzkifQ.M5oANIi2nBtSfIfhyUMqJnex-JYg6Sm92KPYaUL9GKg";
    private static final String ACCEPT_LANGUAGE_HEADER = "nl-NL,en;q=0.8";

    public void beforeRequest(Map<String, List<String>> headers) {
        headers.put("Authorization", Collections.singletonList(AUTH_HEADER));
        headers.put("Accept-Language", Collections.singletonList(ACCEPT_LANGUAGE_HEADER));
    }
}
