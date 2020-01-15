package victor.training.rx;

import rx.Observable;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TimelessOps {
    public static void main(String[] args) {

        Observable<Integer> numbers = Observable.from(Arrays.asList(1, 2, 3, 3, 4, 5,7, 7,2,4,8,2,10,5));
        // TODO distinct
        // TODO contains 7 ?

        Observable.just(1, 1, 2, 2, 3, 3);
        // TODO are all < 4 ?

        Random r = new Random();
        Observable.interval(500, TimeUnit.MILLISECONDS)
                .take(6)
                .map(tick -> r.nextInt(100))
                .doOnNext(n -> System.out.println("Current: " +n))
                // TODO Which is the maximum number ?
                .toBlocking()
                .subscribe(max -> System.out.println("Max: " + max));
    }
}
