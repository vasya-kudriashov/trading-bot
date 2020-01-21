package com.bux.bot.model.pricefeed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
@JsonDeserialize(builder = FeedResponse.FeedResponseBuilder.class)
public class FeedResponse {
    private String type;
    @Builder.Default
    private FeedResponseBody body = FeedResponseBody.builder().build();

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class FeedResponseBuilder {
        @JsonProperty("t")
        private String type;
    }
}
