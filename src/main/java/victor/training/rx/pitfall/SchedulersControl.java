package victor.training.rx.pitfall;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.schedulers.Schedulers;
import victor.training.rx.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;

@Slf4j
public class SchedulersControl {
    public static void main(String[] args) {

        // TODO understand this:
        Observable.defer(() -> f())
                .subscribeOn(Schedulers.io())
                .doOnNext(msg -> log.debug("1:" + msg))
                .observeOn(Schedulers.computation())
                .doOnNext(msg -> log.debug("2:" + msg))
                .map(String::toUpperCase)
                .observeOn(Schedulers.io())
                .doOnNext(msg -> log.debug("3:" + msg))
                .flatMap(SchedulersControl::httpCall)
                .doOnNext(b -> log.debug("4:" + b))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .doOnNext(x -> log.debug("5:" + x))
                .toBlocking()
                .subscribe(b -> log.debug("6:" + b));

        ConcurrencyUtil.sleep(2000);
    }

    private static Observable<String> f() {
        log.debug("f()");
        return Observable.just("a");
    }

    public static Observable<Boolean> httpCall(String s) {
        log.debug("Calling HTTP");
        return Observable.just(s.equals("A"))
                .delay(1, TimeUnit.SECONDS);
    }
}
