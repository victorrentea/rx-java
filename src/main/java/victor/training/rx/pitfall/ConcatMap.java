package victor.training.rx.pitfall;

import rx.Observable;

import java.util.concurrent.TimeUnit;

public class ConcatMap {
    public static void main(String[] args) {
        Observable.just("a","b","c")
            .flatMap(x -> Observable.interval(100, TimeUnit.MILLISECONDS).map(tick->x).take(3))
            .toBlocking()
            .subscribe(System.out::println);
    }
}
