package bux.bot.service;

import bux.bot.model.position.OpenedPosition;
import bux.bot.model.trade.PriceAmount;
import bux.bot.model.trade.Trade;
import bux.bot.service.helper.ClassWithConstructor;
import bux.bot.service.helper.ClassWithConstructorService;
import bux.bot.service.positiion.OpenPositionService;
import com.savoirtech.logging.slf4j.json.logger.Logger;
import mockit.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

public class Resilience4jServiceTest {
    @Tested(fullyInitialized = true)
    private OpenPositionService openPositionService;

    @Tested(fullyInitialized = true)
    private ClassWithConstructorService classWithConstructorService;

    private static SoftAssertions softly = new SoftAssertions();

    @Test
    public void testCallOnSuccess() {
        OpenedPosition expected = OpenedPosition
                .builder()
                .price(PriceAmount
                        .builder()
                        .amount(BigDecimal.ONE)
                        .build())
                .build();
        new MockUp<OpenPositionService>() {
            @Mock
            protected OpenedPosition getFutureSupplier(URI uri) {
                return expected;
            }
        };
        OpenedPosition actual = openPositionService.openPosition(Trade.builder().build());
        softly
                .assertThat(actual)
                .usingRecursiveComparison()
                .ignoringAllOverriddenEquals()
                .isEqualTo(expected);

        softly.assertAll();
    }

    @Test
    public void testCallOnError(@Capturing final Logger logger) {
        new MockUp<OpenPositionService>() {
            @Mock
            protected OpenedPosition getFutureSupplier(URI uri) {
                throw new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
            }
        };

        String responseMessage = "{\"message\":\"message\",\"developerMessage\":\"developerMessage\",\"errorCode\":\"AUTH_007\"}";
        new MockUp<RestClientResponseException>() {
            @Mock
            public String getResponseBodyAsString() {
                return responseMessage;
            }
        };

        new Expectations() {{
            logger
                    .error()
                    .exception("Resilience error", withInstanceLike(new Exception()))
                    .field("URL", anyString)
                    .field("Error message", "message")
                    .field("Developer message", "developerMessage")
                    .field("Error code", "AUTH_007")
                    .log();
        }};

        OpenedPosition actual = openPositionService.openPosition(Trade.builder().build());

        OpenedPosition expected = OpenedPosition
                .builder()
                .build();

        softly
                .assertThat(actual)
                .usingRecursiveComparison()
                .ignoringAllOverriddenEquals()
                .isEqualTo(expected);

        softly.assertAll();
    }

    @Test
    public void testCallOnErrorWithSuccessfulConstructorObjectInstantiation() throws URISyntaxException {
        ClassWithConstructor expected = new ClassWithConstructor();
        new MockUp<ClassWithConstructor>() {
            @Mock
            protected ClassWithConstructor getFutureSupplier(URI uri) {
                throw new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
            }
        };

        ClassWithConstructor actual = classWithConstructorService.call(new URI(""));

        softly
                .assertThat(actual)
                .usingRecursiveComparison()
                .ignoringAllOverriddenEquals()
                .isEqualTo(expected);

        softly.assertAll();
    }
}
