package bux.bot.model.position;

import bux.bot.model.trade.PriceAmount;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonDeserialize(builder = OpenedPosition.OpenedPositionBuilder.class)
public class OpenedPosition extends Position {
    @Builder
    public OpenedPosition(String id,
                          String positionId,
                          PositionProduct product,
                          PriceAmount investingAmount,
                          PriceAmount price,
                          int leverage,
                          String direction,
                          String type,
                          String dateCreated
    ) {
        super(id, positionId, product, investingAmount, price, leverage, direction, type, dateCreated);
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class OpenedPositionBuilder { }
}
