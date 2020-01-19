package bux.bot.model.general;

import java.util.Arrays;

public enum TradingProduct {
    GERMANY_30("sb26493"),
    US_500("sb26496"),
    EUR_USD("sb26502"),
    GOLD("sb26500"),
    APPLE("sb26513"),
    DEUTSCHE_BANK("sb28248"),
    UNKNOWN("0");

    private String id;

    public String getId() {
        return id;
    }

    TradingProduct(String id) {
        this.id = id;
    }

    public String getRequestName() {
        return String.format("trading.product.%s", id);
    }

    public static TradingProduct getById(String id) {
        return Arrays
                .stream(TradingProduct.values())
                .filter(e -> e.id.equals(id))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
