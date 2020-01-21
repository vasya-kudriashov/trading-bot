package bux.bot.service.pricefeed;

import bux.bot.model.shared.TradingData;
import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.Logger;
import org.springframework.web.util.UriComponentsBuilder;

import javax.websocket.ContainerProvider;
import java.util.concurrent.CountDownLatch;

public class FeedService {
    private static final String SERVICE_RESOURCE = "/subscriptions/me";
    private static final String SERVICE_HOST = "wss://rtf.beta.getbux.com";
    private static Logger logger = LoggerFactory.getLogger(FeedService.class);

    public void start(TradingData tradingData) {
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
                            UriComponentsBuilder
                                    .fromUriString(SERVICE_HOST)
                                    .path(SERVICE_RESOURCE)
                                    .build()
                                    .encode()
                                    .toUri()
                    );
            latch.await();
        } catch (Exception e) {
            System.out.println("Error during connection to price feed occurred.");
            logger
                    .error()
                    .exception("Error during connection to price feed occurred.", e)
                    .log();
        }
    }
}
