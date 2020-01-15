package victor.training.rx.exercise;

import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class RxTesting {
    @Test
    public void items() {
        Observable<String> stringsObs = Observable.from("abc".split(""))
                .zipWith(Observable.range(0, Integer.MAX_VALUE),
                        (s, i) -> i + ":" + s);

        // TODO gather & assert
        // TODO TestSubscriber
    }

    @Test
    public void error() {
        Observable<String> stringsObs = Observable.from("abc".split(""))
                .zipWith(Observable.range(0, Integer.MAX_VALUE),
                        (s, i) -> i + ":" + s)
                .concatWith(Observable.error(new IllegalStateException("hah")));

        // TODO TestSubscriber
    }

    @Test
    public void timeBound() {
        Observable<String> stringsObs = Observable.from("abc".split(""))
                .zipWith(Observable.interval(1, TimeUnit.SECONDS), // !!!
                        (s, i) -> i + ":" + s);

        // TODO TestScheduler + TestSubscriber

        // after 500ms: []
        // after 1100ms: [0:a]
        // after 3000ms: [0:a,1:b,2:c]
    }


}
