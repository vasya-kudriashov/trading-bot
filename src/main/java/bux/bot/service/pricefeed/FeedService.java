package bux.bot.service.pricefeed;

import bux.bot.model.shared.TradingData;
import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.Logger;
import org.springframework.web.util.UriComponentsBuilder;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

public class FeedService {
    private static final String SERVICE_RESOURCE = "/subscriptions/me";
    private static final String SERVICE_HOST = "ws://localhost:8080";//"wss://rtf.beta.getbux.com";
    private static Logger logger = LoggerFactory.getLogger(FeedService.class);

    public static void start(TradingData tradingData, CountDownLatch latch) {
        URI uri = UriComponentsBuilder
                .fromUriString(SERVICE_HOST)
                .path(SERVICE_RESOURCE)
                .build()
                .encode()
                .toUri();

        try {
            ContainerProvider
                    .getWebSocketContainer()
                    .connectToServer(
                            FeedServiceClient
                                    .builder()
                                    .tradingData(tradingData)
                                    .latch(latch)
                                    .build(),
                            uri
                    );
        } catch (Exception e) {
            System.out.println("Connection failed.");
            logger
                    .error()
                    .exception("Feed service error", e)
                    .field("URL", uri.toString())
                    .log();
        }
    }
}
