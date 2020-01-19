package bux.bot.model.trade;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class Trade {
    private int leverage;
    private String direction;
    @Builder.Default
    private PriceAmount investingAmount = PriceAmount.builder().build();
    @Builder.Default
    private Source source = Source.builder().build();
    private String productId;
}
