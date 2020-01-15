package victor.training.rx.exercise;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.Single;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RetryUntilOK {

    public static void main(String[] args) {
        log.debug("Start");
        // TODO: Retry max 3 times, with 1 sec between them. Each retry needs a sequential index
        // TODO stop at first OK response: take(1) or first()?
        // TODO error handling
        // TODO more Observable adoption


    }

    public static Single<Response> getDeliveryEstimation(int retryIndex) {
        log.debug("GET /url/order?retry=" + retryIndex);
        switch (new Random().nextInt(2)) {
            // TODO try throwing errors (set 3 as bound above) -> kills the Observable
            case 0:
                log.debug("Return 200");
                return Single.just(new Response(200, "DATA"));
            case 1:
                log.debug("Return 500");
                return Single.just(new Response(500, ""));
            case 2:
                log.debug("Return Timeout");
                return Single.error(new RuntimeException("Connection timed out"));
            default:
                throw new IllegalStateException("Unexpected value: " + new Random().nextInt(3));
        }
    }
}

@Value
class Response {
    int statusCode;
    String body;
}