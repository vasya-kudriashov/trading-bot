package bux.bot.model.position;

import bux.bot.model.trade.PriceAmount;
import lombok.Getter;

@Getter
public abstract class Position {
    private String id;
    private String positionId;
    private PositionProduct product;
    private PriceAmount investingAmount;
    private PriceAmount price;
    private int leverage;
    private String direction;
    private String type;
    private String dateCreated;

    public Position(String id, String positionId, PositionProduct product, PriceAmount investingAmount, PriceAmount price, int leverage, String direction, String type, String dateCreated) {
        this.id = id;
        this.positionId = positionId;
        this.product = product != null ? product : PositionProduct.builder().build();
        this.investingAmount = investingAmount != null ? investingAmount : PriceAmount.builder().build();
        this.price = price != null ? price : PriceAmount.builder().build();
        this.leverage = leverage;
        this.direction = direction;
        this.type = type;
        this.dateCreated = dateCreated;
    }
}
