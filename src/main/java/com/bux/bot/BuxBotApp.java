package com.bux.bot;

import com.bux.bot.model.shared.TradingData;
import com.bux.bot.service.data.TradingDataReader;
import com.bux.bot.service.pricefeed.FeedService;

public class BuxBotApp {
    public static void main(String[] args) {
        TradingData tradingData = TradingDataReader.readFromConsole();
        new FeedService().start(tradingData);
    }
}
