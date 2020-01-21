package com.bux.bot.model.shared;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder
public class TradingData {
    private TradingProduct tradingProduct;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal stopLossPrice;
}
