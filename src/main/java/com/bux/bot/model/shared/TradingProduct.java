package com.bux.bot.model.shared;

import java.util.Arrays;
import java.util.Optional;

public enum TradingProduct {
    GERMANY_30("sb26493", 1),
    US_500("sb26496", 2),
    EUR_USD("sb26502", 5),
    GOLD("sb26500", 1),
    APPLE("sb26513", 2),
    DEUTSCHE_BANK("sb28248", 2);

    private String id;
    private int decimals;

    TradingProduct(String id, int decimals) {
        this.id = id;
        this.decimals = decimals;
    }

    public String getRequestName() {
        return String.format("trading.product.%s", id);
    }

    public static Optional<TradingProduct> getById(String id) {
        return Arrays
                .stream(TradingProduct.values())
                .filter(e -> e.id.equals(id))
                .findFirst();
    }

    public String getId() {
        return id;
    }

    public int getDecimals() {
        return decimals;
    }
}
