package victor.training.rx.pitfall;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import victor.training.rx.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TheMostComplexMarble {

    private static Observable<Integer> fibonacciObservable() {
        // TODO: implement an infinite fibonacci observable
        return Observable.just(1);
    }

    public static void main(String[] args) {
        fibonacciObservable()
                .take(30)
                .toList()
                .subscribe(System.out::println);


        long t0 = System.currentTimeMillis();
        Observable.defer(() -> {
                    log.debug("Calling you...");
                    if (System.currentTimeMillis() - t0 > 3_000) {
                        return Observable.just("data");
                    } else {
                        return Observable.error(new RuntimeException("Connection Error"));
                    }
                })
                // TODO retry with a fibonacci backoff delay

                .toBlocking()
                .subscribe(System.out::println);
    }

}
