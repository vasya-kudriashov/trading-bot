package bux.bot.service.pricefeed;

import bux.bot.model.shared.TradingData;
import bux.bot.model.position.ClosedPosition;
import bux.bot.model.position.OpenedPosition;
import bux.bot.model.pricefeed.FeedRequest;
import bux.bot.model.pricefeed.FeedResponse;
import bux.bot.model.pricefeed.FeedEventType;
import bux.bot.model.trade.PriceAmount;
import bux.bot.model.trade.Source;
import bux.bot.model.trade.Trade;
import bux.bot.service.positiion.ClosePositionService;
import bux.bot.service.positiion.OpenPositionService;
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
    private Boolean isMarketAboveBuyPriceOnStart;
    private OpenedPosition openedPosition = OpenedPosition.builder().build();

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
        FeedResponse response = FeedResponse.builder().build();
        try {
            response = objectMapper.readValue(message, FeedResponse.class);
        } catch (IOException e) {
            logger
                    .error()
                    .exception("Feed message convert error", e)
                    .field("message", message)
                    .log();
        }

        if (response.getType() != null) {
            switch (FeedEventType.getByType(response.getType())) {
                case CONNECT_CONNECTED:
                    subscribeToFeed();
                    break;
                case TRADING_QUOTE:
                    handleTradingQuote(response);
                    break;
                case CONNECT_FAILED:
                    handleFailedConnection(response);
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
                        tradingData.getTradingProduct().getRequestName())
                )
                .build();

        session
                .getBasicRemote()
                .sendText(objectMapper.writeValueAsString(request));
    }

    private void handleTradingQuote(FeedResponse response) {
        BigDecimal currentPrice = response.getBody().getCurrentPrice();

        printCurrentState(response.getBody().getSecurityId(), currentPrice);

        if (isMarketAboveBuyPriceOnStart == null) {
            isMarketAboveBuyPriceOnStart = currentPrice.compareTo(tradingData.getOpenPrice()) > 0;
        }

        if (openedPosition.getPrice().getAmount() == null) { //isOpenPositionCaseFulfilled(currentPrice)) {
            openPosition();
        } else if (openedPosition.getPrice().getAmount().add(BigDecimal.ONE).compareTo(currentPrice) < 0) {//isClosePositionCaseFulfilled(currentPrice)) {
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
                    && (isMarketAboveBuyPriceOnStart // do we open position following a trend or against it?
                    ? price.compareTo(tradingData.getOpenPrice()) <= 0
                    : price.compareTo(tradingData.getOpenPrice()) >= 0);
    }

    private void openPosition() {
        openedPosition = openPositionService.openPosition(getTrade());

        if (openedPosition.getPrice().getAmount() != null) {
            System.out.println(String.format(
                    "New position has been opened at price level %s%s and invested %s%s",
                    openedPosition.getPrice().getAmount(),
                    openedPosition.getPrice().getCurrency(),
                    openedPosition.getInvestingAmount().getAmount(),
                    openedPosition.getInvestingAmount().getCurrency()
            ));
        }
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