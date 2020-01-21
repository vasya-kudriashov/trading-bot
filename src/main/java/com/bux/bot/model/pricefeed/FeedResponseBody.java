package com.bux.bot.model.pricefeed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@JsonDeserialize(builder = FeedResponseBody.FeedResponseBodyBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedResponseBody {
    private String securityId;
    private BigDecimal currentPrice;
    private String developerMessage;
    private String errorCode;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class FeedResponseBodyBuilder {}
}
