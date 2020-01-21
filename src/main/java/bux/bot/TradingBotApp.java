package bux.bot;

import bux.bot.model.shared.TradingData;
import bux.bot.service.data.TradingDataReader;
import bux.bot.service.pricefeed.FeedService;

public class TradingBotApp {
    public static void main(String[] args) {
        TradingData tradingData = TradingDataReader.readFromConsole();
        new FeedService().start(tradingData);
    }
}
