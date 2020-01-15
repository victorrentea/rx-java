package victor.training.rx.exercise;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.Subscriber;
import victor.training.rx.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;

import static victor.training.rx.ConcurrencyUtil.log;
import static victor.training.rx.ConcurrencyUtil.sleep;

@Slf4j
public class SubscriberVsObserver {
    public static void main(String[] args) {
        Subscriber<Long> subscriber = new MySubscriber();
        Observable.interval(500, TimeUnit.MILLISECONDS)
            .doOnUnsubscribe(() -> log.debug("Unsubscribed"))
            .subscribe(subscriber);
        sleep(1600);

        log.debug("Calling .unsubscribe");
        subscriber.unsubscribe();
        // TODO: use Observer instead
        // TODO: use an Action instead

        sleep(1000);
        log.debug("END");
    }

    private static class MySubscriber extends Subscriber<Long> {
        @Override
        public void onCompleted() {
            log.debug("DONE");
        }

        @Override
        public void onError(Throwable e) {
            log.debug("ERR");
        }

        @Override
        public void onNext(Long aLong) {
            log.debug("ELEM:" + aLong);
        }
    }
}
