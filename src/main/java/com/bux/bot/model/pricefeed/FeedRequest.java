package com.bux.bot.model.pricefeed;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter @Builder
public class FeedRequest {
    @Builder.Default
    private List<String> subscribeTo = new ArrayList<>();
    @Builder.Default
    private List<String> unsubscribeFrom = new ArrayList<>();
}
