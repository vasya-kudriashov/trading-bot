package com.bux.bot.service.positiion;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class HeaderRequestInterceptor implements ClientHttpRequestInterceptor {
    private final String name;
    private final String value;

    public HeaderRequestInterceptor(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set(name, value);
        return execution.execute(request, body);
    }
}