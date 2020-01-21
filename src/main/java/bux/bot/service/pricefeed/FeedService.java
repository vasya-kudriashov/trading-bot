package bux.bot.service.pricefeed;

import bux.bot.model.shared.TradingData;
import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.Logger;
import org.springframework.web.util.UriComponentsBuilder;

import javax.websocket.ContainerProvider;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

public class FeedService {
    private static final String SERVICE_RESOURCE = "/subscriptions/me";
    private static final String SERVICE_HOST = "ws://localhost:8080";//"wss://rtf.beta.getbux.com";
    private static Logger logger = LoggerFactory.getLogger(FeedService.class);

    public void start(TradingData tradingData) {
        URI uri = UriComponentsBuilder
                .fromUriString(SERVICE_HOST)
                .path(SERVICE_RESOURCE)
                .build()
                .encode()
                .toUri();

        CountDownLatch latch = new CountDownLatch(1);
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
            latch.await();
        } catch (Exception e) {
            System.out.println("Error during connection occurred.");
            logger
                    .error()
                    .exception("Error during connection occurred.", e)
                    .log();
        }
    }
}
