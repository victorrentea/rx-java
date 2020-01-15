package victor.training.rx;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Zip {
    public static void main(String[] args) {
        log.debug("Start");
        httpTooFast(1)
                // TODO: delay the response to be more credible :))) Users don't trust such a fast search
                .zipWith(Observable.timer(2,TimeUnit.SECONDS),(s,zero)->s)
                .subscribe(s -> log.debug("Got results: " +s));

        ConcurrencyUtil.sleep(4000);
    }

    public static Observable<String> httpTooFast(long movieId) {
        return Observable.just("search results").delay(50, TimeUnit.MILLISECONDS);
    }
}
