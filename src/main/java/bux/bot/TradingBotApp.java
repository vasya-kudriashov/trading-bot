package bux.bot;

import bux.bot.service.data.TradingDataReader;
import bux.bot.service.pricefeed.FeedService;
import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.Logger;

import java.util.concurrent.CountDownLatch;

public class TradingBotApp {
    private static Logger logger = LoggerFactory.getLogger(TradingBotApp.class);

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            FeedService.start(TradingDataReader.readFromConsole(), latch);
            latch.await();
        } catch (InterruptedException e) {
            System.out.println("Connection failed.");
            logger
                    .error()
                    .exception("An error occurred.", e)
                    .log();
        }
    }
}
