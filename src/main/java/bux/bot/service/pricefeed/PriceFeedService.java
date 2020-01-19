package bux.bot.service.pricefeed;

import bux.bot.model.general.TradingData;
import bux.bot.model.position.ClosedPosition;
import bux.bot.model.position.OpenedPosition;
import bux.bot.model.pricefeed.PriceFeedRequest;
import bux.bot.model.pricefeed.PriceFeedResponse;
import bux.bot.model.pricefeed.PriceFeedServiceEventType;
import bux.bot.model.trade.PriceAmount;
import bux.bot.model.trade.Source;
import bux.bot.model.trade.Trade;
import bux.bot.service.positiion.ClosePositionService;
import bux.bot.service.positiion.OpenPositionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;

import javax.websocket.*;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint(configurator = PriceFeedServiceConfigurator.class)
public class PriceFeedService {
    private CountDownLatch latch;
    private TradingData tradingData;
    private Session session;
    private OpenedPosition openedPosition = OpenedPosition.builder().build();
    private Boolean isMarketAboveBuyPriceOnStart;

    private OpenPositionService openPositionService = new OpenPositionService();
    private ClosePositionService closePositionService = new ClosePositionService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Builder
    public PriceFeedService(TradingData tradingData, CountDownLatch latch) {
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
        PriceFeedResponse response = objectMapper.readValue(message, PriceFeedResponse.class);

        if (response.getType() != null) {
            switch (PriceFeedServiceEventType.getByType(response.getType())) {
                case CONNECT_CONNECTED:
                    subscribeToFeed();
                    break;
                case TRADING_QUOTE:
                    handleTradingQuote(response);
                    break;
                case CONNECT_FAILED:
                    System.out.println("Connection failed.");
                    break;
                default:
                    System.out.println("Unknown response type.");
            }
        }
    }

    private void subscribeToFeed() throws IOException {
        PriceFeedRequest request = PriceFeedRequest
                .builder()
                .subscribeTo(Collections.singletonList(
                        tradingData.getTradingProduct().getRequestName())
                )
                .build();

        session
                .getBasicRemote()
                .sendText(objectMapper.writeValueAsString(request));
    }

    private void handleTradingQuote(PriceFeedResponse response) {
        float currentPrice = response.getBody().getCurrentPrice();

        printCurrentState(response.getBody().getSecurityId(), currentPrice);

        if (isMarketAboveBuyPriceOnStart == null) {
            isMarketAboveBuyPriceOnStart = currentPrice > tradingData.getOpenPrice();
        }

        if (openPosition(currentPrice)) {
            openPosition();
        } else if (closePosition(currentPrice)) {
            closePosition();
            latch.countDown();
        }
    }

    private void printCurrentState(String securityId, float currentPrice) {
        System.out.println(String.format(
                "%s price: %s\tStatus: %s",
                securityId,
                currentPrice,
                (openedPosition.getPrice().getAmount() > 0)
                        ? "Opened at " + openedPosition.getPrice().getAmount()
                        : "On Hold"
        ));
    }

    private boolean openPosition(float price) {
        return (openedPosition.getPrice().getAmount() == 0)
                && (isMarketAboveBuyPriceOnStart // depend on if we open position following a trend or against it
                ? price <= tradingData.getOpenPrice()
                : price >= tradingData.getOpenPrice());
    }

    private void openPosition() {
        openedPosition = openPositionService.openPosition(getTrade());

        System.out.println(String.format(
                "New position has been opened at price level %s%s and invested %s%s",
                openedPosition.getPrice().getAmount(),
                openedPosition.getPrice().getCurrency(),
                openedPosition.getInvestingAmount().getAmount(),
                openedPosition.getInvestingAmount().getCurrency()
        ));
    }

    private Trade getTrade() {
        return Trade
                    .builder()
                    .leverage(2)
                    .direction("BUY")
                    .investingAmount(
                            PriceAmount
                                    .builder()
                                    .currency("BUX")
                                    .amount(10)
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

    private boolean closePosition(float price) {
        return (openedPosition.getPrice().getAmount() > 0)
                && ((price >= tradingData.getClosePrice()) || (price <= tradingData.getStopLossPrice())
        );
    }

    private void closePosition() {
        ClosedPosition closedPosition = closePositionService.closePosition(openedPosition.getPositionId());

        System.out.println(String.format(
                "Position has been closed at price level %s%s with %s %s%s",
                closedPosition.getPrice().getAmount(),
                closedPosition.getPrice().getCurrency(),
                (closedPosition.getProfitAndLoss().getAmount() > 0) ? "profit" : "loss",
                closedPosition.getProfitAndLoss().getAmount(),
                closedPosition.getProfitAndLoss().getCurrency()
        ));
    }

    @OnClose
    public void onClose(Session session, CloseReason reason){
        System.out.println("Disconnected.");
    }

    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("Error occurred: " + error.getMessage());
    }
}
