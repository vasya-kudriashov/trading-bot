package com.bux.bot.model.position;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
@JsonDeserialize(builder = PositionProduct.PositionProductBuilder.class)
public class PositionProduct {
    private String securityId;
    private String symbol;
    private String displayName;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PositionProductBuilder {}
}
