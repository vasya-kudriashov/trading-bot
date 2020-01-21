package com.bux.bot.model.trade;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

// TODO Consider some ready-made currency domain, e.g. CurrencyUnit
@Getter @Builder
@JsonDeserialize(builder = PriceAmount.PriceAmountBuilder.class)
public class PriceAmount {
    private String currency;
    private int decimals;
    private BigDecimal amount;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PriceAmountBuilder { }
}
