package bux.bot.service.pricefeed;

import bux.bot.model.general.TradingData;
import org.springframework.web.util.UriComponentsBuilder;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class TradingBot {
    private static final String SERVICE_RESOURCE = "/subscriptions/me";
    private static final String SERVICE_HOST = "ws://localhost:8080";

    public static void start(TradingData tradingData, CountDownLatch latch) throws IOException, DeploymentException {
        ContainerProvider
                .getWebSocketContainer()
                .connectToServer(
                        PriceFeedService
                                .builder()
                                .tradingData(tradingData)
                                .latch(latch)
                                .build(),
                        UriComponentsBuilder
                                .fromUriString(SERVICE_HOST)
                                .path(SERVICE_RESOURCE)
                                .build()
                                .encode()
                                .toUri()
                );
    }
}
