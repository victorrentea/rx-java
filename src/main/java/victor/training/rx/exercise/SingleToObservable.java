package victor.training.rx.exercise;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.Single;

import java.util.concurrent.TimeUnit;

@Slf4j
public class SingleToObservable {
    public static void main(String[] args) {

        Single<Integer> single = Single.just(1);

        // TODO create an Observable that emits the value of the single 10 times.

        log.debug("Start");
        Single<String> s1 = Single.just("a").delay(1, TimeUnit.SECONDS);
        Single<String> s2 = Single.just("b").delay(2, TimeUnit.SECONDS);

        // TODO create an Observable that emits a and b ASAP
        // TODO emits them at once a+b
    }
}
