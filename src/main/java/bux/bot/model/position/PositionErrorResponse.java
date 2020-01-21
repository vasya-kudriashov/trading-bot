package bux.bot.model.position;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
@JsonDeserialize(builder = PositionErrorResponse.PositionErrorResponseBuilder.class)
public class PositionErrorResponse {
    private String message;
    private String developerMessage;
    private String errorCode;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PositionErrorResponseBuilder { }
}
