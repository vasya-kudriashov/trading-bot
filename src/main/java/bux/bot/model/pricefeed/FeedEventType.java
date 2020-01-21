package bux.bot.model.pricefeed;

public enum FeedEventType {
    CONNECT_CONNECTED("connect.connected"),
    CONNECT_FAILED("connect.failed"),
    TRADING_QUOTE("trading.quote"),
    UNKNOWN("");

    private String type;

    FeedEventType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static FeedEventType getByType(String type) {
        if (type.equals(CONNECT_CONNECTED.getType())) {
            return CONNECT_CONNECTED;
        } else if (type.equals(CONNECT_FAILED.getType())) {
            return CONNECT_FAILED;
        } else if (type.equals(TRADING_QUOTE.getType())) {
            return TRADING_QUOTE;
        } else {
            return UNKNOWN;
        }
    }
}
