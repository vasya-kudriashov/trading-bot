package bux.bot;

import bux.bot.service.data.TradingDataReader;
import bux.bot.service.pricefeed.TradingBot;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class TradingBotApp {
    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            TradingBot.start(TradingDataReader.readFromConsole(), latch);
            latch.await();
        } catch (IOException | DeploymentException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
