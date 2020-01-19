package bux.bot.model.general;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class TradingData {
    private TradingProduct tradingProduct;
    private float openPrice;
    private float closePrice;
    private float stopLossPrice;
}
