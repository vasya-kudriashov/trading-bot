package com.bux.bot.service.pricefeed;

import com.bux.bot.model.exception.FailedPositionOpeningException;
import com.bux.bot.model.shared.TradingData;
import com.bux.bot.model.position.ClosedPosition;
import com.bux.bot.model.position.OpenedPosition;
import com.bux.bot.model.pricefeed.FeedRequest;
import com.bux.bot.model.pricefeed.FeedResponse;
import com.bux.bot.model.pricefeed.FeedEventType;
import com.bux.bot.model.trade.PriceAmount;
import com.bux.bot.model.trade.Source;
import com.bux.bot.model.trade.Trade;
import com.bux.bot.service.positiion.ClosePositionService;
import com.bux.bot.service.positiion.OpenPositionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.Logger;
import lombok.Builder;

import javax.websocket.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint(configurator = FeedServiceConfigurator.class)
public class FeedServiceClient {
    private static Logger logger = LoggerFactory.getLogger(FeedServiceClient.class);
    private static OpenPositionService openPositionService = new OpenPositionService();
    private static ClosePositionService closePositionService = new ClosePositionService();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private Session session;
    private CountDownLatch latch;
    private TradingData tradingData;
    private OpenedPosition openedPosition = OpenedPosition.builder().build();
    private Boolean isMarketAboveBuyPriceOnStart;

    @Builder
    public FeedServiceClient(TradingData tradingData, CountDownLatch latch) {
        this.tradingData = tradingData;
        this.latch = latch;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connected.");
    }

    @OnMessage
    public void onMessage(String message) throws IOException {
        FeedResponse response = objectMapper.readValue(message, FeedResponse.class);

        if (response != null && response.getType() != null) {
            switch (FeedEventType.getByType(response.getType())) {
                case CONNECT_CONNECTED:
                    subscribeToFeed();
                    break;
                case CONNECT_FAILED:
                    handleFailedConnection(response);
                    break;
                case TRADING_QUOTE:
                    handleTradingQuote(response);
                    break;
                default:
                    break;
            }
        }
    }

    private void subscribeToFeed() throws IOException {
        FeedRequest request = FeedRequest
                .builder()
                .subscribeTo(Collections.singletonList(
                        tradingData
                                .getTradingProduct()
                                .getRequestName())
                )
                .build();

        sendText(objectMapper.writeValueAsString(request));
    }

    private void sendText(String text) throws IOException {
        session
                .getBasicRemote()
                .sendText(text);
    }

    private void handleTradingQuote(FeedResponse response) {
        BigDecimal currentPrice = response.getBody().getCurrentPrice();

        printCurrentState(response.getBody().getSecurityId(), currentPrice);

        if (isMarketAboveBuyPriceOnStart == null) {
            isMarketAboveBuyPriceOnStart = currentPrice.compareTo(tradingData.getOpenPrice()) > 0;
        }

        if (isOpenPositionCaseFulfilled(currentPrice)) {
            openPosition();
        } else if (isClosePositionCaseFulfilled(currentPrice)) {
            closePosition();
            latch.countDown();
        }
    }

    private void printCurrentState(String securityId, BigDecimal currentPrice) {
        System.out.println(String.format(
                "%s price: %s\tStatus: %s",
                securityId,
                currentPrice,
                (openedPosition.getPrice().getAmount() != null)
                        ? "Opened at " + openedPosition.getPrice().getAmount()
                        : "On Hold"
        ));
    }

    private boolean isOpenPositionCaseFulfilled(BigDecimal price) {
        return (openedPosition.getPrice().getAmount() == null)
                    && (isMarketAboveBuyPriceOnStart // if we open position following a trend or against it
                    ? price.compareTo(tradingData.getOpenPrice()) <= 0
                    : price.compareTo(tradingData.getOpenPrice()) >= 0);
    }

    private void openPosition() {
        openedPosition = openPositionService.openPosition(getTrade());

        /*
         * If something goes wrong with position opening, so market goes away
         * and input data is not relevant anymore, stop everything
         */
        if (openedPosition.getPrice().getAmount() == null) {
            handleFailedPositionOpening();

            latch.countDown();
        }

        System.out.println(String.format(
                "New position has been opened at price level %s%s and invested %s%s",
                openedPosition.getPrice().getAmount(),
                openedPosition.getPrice().getCurrency(),
                openedPosition.getInvestingAmount().getAmount(),
                openedPosition.getInvestingAmount().getCurrency()
        ));
    }

    private void handleFailedPositionOpening() {
        System.out.println("An error during position's opening occurred.");
        logger
                .error()
                .exception(
                        "An error during position's opening occurred.",
                        new FailedPositionOpeningException()
                )
                .log();
    }

    // Example trade object with sample data
    private Trade getTrade() {
        return Trade
                    .builder()
                    .leverage(2)
                    .direction("BUY")
                    .investingAmount(
                            PriceAmount
                                    .builder()
                                    .currency("BUX")
                                    .amount(BigDecimal.valueOf(10))
                                    .decimals(2)
                                    .build()
                    )
                    .source(
                            Source
                                    .builder()
                                    .sourceType("OTHER")
                                    .build()
                    )
                    .productId(tradingData.getTradingProduct().getId())
                    .build();
    }
    private boolean isClosePositionCaseFulfilled(BigDecimal price) {
        return (openedPosition.getPrice().getAmount() != null)
                    && ((price.compareTo(tradingData.getClosePrice()) >= 0)
                        || (price.compareTo(tradingData.getStopLossPrice()) <= 0)
        );
    }

    private void closePosition() {
        ClosedPosition closedPosition = ClosedPosition.builder().build();

        /*
         * try to close opened position as many times as possible in a case of error during closing,
         * in meanwhile developers will be annoyed via ClosePositionService logging that something goes wrong!
         */
        while (closedPosition.getPrice().getAmount() == null) {
            closedPosition = closePositionService.closePosition(openedPosition.getPositionId());
        }

        System.out.println(String.format(
                "Position has been closed at price level %s%s with %s %s%s",
                closedPosition.getPrice().getAmount(),
                closedPosition.getPrice().getCurrency(),
                (closedPosition.getProfitAndLoss().getAmount().compareTo(BigDecimal.valueOf(0)) > 0)
                        ? "profit"
                        : "loss",
                closedPosition.getProfitAndLoss().getAmount(),
                closedPosition.getProfitAndLoss().getCurrency()
        ));
    }

    private void handleFailedConnection(FeedResponse response) {
        System.out.println("Connection failed.");
        logger
                .error()
                .message("Connection failed.")
                .field("Error code", response.getBody().getErrorCode())
                .field("Message", response.getBody().getDeveloperMessage())
                .log();

        latch.countDown();
    }

    @OnClose
    public void onClose(Session session, CloseReason reason){
        System.out.println("Disconnected.");
    }

    @OnError
    public void onError(Session session, Throwable throwable){
        System.out.println("Connection failed.");
        logger
                .error()
                .exception("Connection failed.", new Exception(throwable))
                .log();
    }
}
