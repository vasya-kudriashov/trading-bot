package bux.bot.model.pricefeed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = PriceFeedResponseBody.PriceFeedResponseBodyBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceFeedResponseBody {
    private String securityId;
    private float currentPrice;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class PriceFeedResponseBodyBuilder {}
}
