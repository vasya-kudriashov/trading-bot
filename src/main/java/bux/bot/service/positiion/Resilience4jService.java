package bux.bot.service.positiion;

import bux.bot.model.position.PositionErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.Logger;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.vavr.control.Try;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class Resilience4jService<T> {
    private static Logger logger = LoggerFactory.getLogger(Resilience4jService.class);
    private static ObjectMapper objectMapper = new ObjectMapper();
    private int timeout = 2000;

    @SuppressWarnings("unchecked")
    private Class<T> clazz = (Class<T>)((ParameterizedType) this.getClass().getGenericSuperclass())
            .getActualTypeArguments()[0];

    public T call(URI uri) {
        return Try.of(doRequest(uri)::call)
                .map(result -> result == null
                        ? initResultObject(uri.toString())
                        : result
                )
                .recover(throwable -> fallback(throwable, uri.toString()))
                .get();
    }

    private Callable<T> doRequest(URI uri) {
        return CircuitBreaker.decorateCallable(
                CircuitBreaker.ofDefaults("default"),
                TimeLimiter.decorateFutureSupplier(getTimeLimiter(), () -> getFutureResult(uri))
        );
    }

    private TimeLimiter getTimeLimiter() {
        return TimeLimiter.of(
                TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofMillis(timeout))
                        .build()
        );
    }

    private Future<T> getFutureResult(URI uri) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<T> futureResult = executorService.submit(() -> getFutureSupplier(uri));
        executorService.shutdown();

        return futureResult;
    }

    protected T getFutureSupplier(URI uri) {
        return new RestTemplate().getForObject(uri, clazz);
    }

    protected T fallback(Throwable throwable, String url) {
        PositionErrorResponse responseOnError = PositionErrorResponse
                .builder()
                .build();

        if (throwable != null && throwable.getCause() != null) {
            String responseBody = ((HttpClientErrorException) throwable
                    .getCause())
                    .getResponseBodyAsString();

            try {
                responseOnError = objectMapper.readValue(responseBody, PositionErrorResponse.class);
            } catch (IOException e) {
                logger
                        .error()
                        .exception(e.getMessage(), e)
                        .log();
            }
        }

        logger
                .error()
                .exception("Resilience error", new Exception(throwable))
                .field("URL", url)
                .field("Error message", responseOnError.getMessage())
                .field("Developer message", responseOnError.getDeveloperMessage())
                .field("Error code", responseOnError.getErrorCode())
                .log();

        return initResultObject(url);
    }

    @SuppressWarnings("unchecked")
    private T initResultObject(String url) {
        T result = null;
        try {
            Object builder = clazz
                    .getMethod("builder")
                    .invoke(null);

            result = (T) builder
                    .getClass()
                    .getMethod("build")
                    .invoke(builder);
        } catch (NoSuchMethodException e) {
            try {
                result = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e1) {
                logger
                        .error()
                        .exception("Resilience error", e1)
                        .field("URL", url)
                        .log();
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger
                    .error()
                    .exception("Resilience error", e)
                    .field("URL", url)
                    .log();
        }

        return result;
    }
}
