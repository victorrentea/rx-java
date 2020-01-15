package victor.training.rx.pitfall;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import victor.training.rx.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TheMostComplexMarble {

    private static Observable<Integer> fibonacciObservable() {
        // TODO: implement an infinite fibonacci observable
        return Observable.just(1)
                .repeat()
                .scan(new int[]{1,1}, (arr, i) -> new int[]{arr[1], arr[0] + arr[1]})
                .map(arr -> arr[0]);
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
                .retryWhen(attempts ->
                        attempts.zipWith(fibonacciObservable(), (e, fib) -> fib)
                                .flatMap(fib -> {
                                    log.debug("Delaying retry by "+ fib + " ms");
                                    return Observable.timer(fib, TimeUnit.MILLISECONDS);
                                }))

                .subscribe(System.out::println);

        ConcurrencyUtil.sleep(5000);
    }

}
