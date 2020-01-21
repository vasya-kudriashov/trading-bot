package com.bux.bot.model.position;

import com.bux.bot.model.trade.PriceAmount;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonDeserialize(builder = ClosedPosition.ClosedPositionBuilder.class)
public class ClosedPosition extends Position {
    private PriceAmount profitAndLoss;

    @Builder
    public ClosedPosition(String id,
                          String positionId,
                          PositionProduct product,
                          PriceAmount investingAmount,
                          PriceAmount price,
                          int leverage,
                          String direction,
                          String type,
                          String dateCreated,
                          PriceAmount profitAndLoss
    ) {
        super(id, positionId, product, investingAmount, price, leverage, direction, type, dateCreated);

        this.profitAndLoss = profitAndLoss != null ? profitAndLoss : PriceAmount.builder().build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ClosedPositionBuilder { }
}
