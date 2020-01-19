package bux.bot.model.pricefeed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
@JsonDeserialize(builder = PriceFeedResponse.PriceFeedResponseBuilder.class)
public class PriceFeedResponse {
    private String type;
    @Builder.Default
    private PriceFeedResponseBody body = PriceFeedResponseBody.builder().build();

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class PriceFeedResponseBuilder {
        @JsonProperty("t")
        private String type;
    }
}
