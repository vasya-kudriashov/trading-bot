package bux.bot.service.positiion;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class ResilienceService<T> {
    private int timeout = 2000;
    private static Logger logger = LoggerFactory.getLogger(ResilienceService.class);

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
        throwable.printStackTrace();
        logger.error("Resilience error", throwable);

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
                e1.printStackTrace();
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return result;
    }
}
