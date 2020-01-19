package bux.bot.model.trade;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
@JsonDeserialize(builder = PriceAmount.PriceAmountBuilder.class)
public class PriceAmount {
    private String currency;
    private int decimals;
    private float amount;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PriceAmountBuilder { }
}
