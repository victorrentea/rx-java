package victor.training.rx.exercise;

import rx.Observable;
import rx.Subscriber;
import victor.training.rx.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;

import static victor.training.rx.ConcurrencyUtil.log;

public class SubscriberVsObserver {
    public static void main(String[] args) {
        Subscriber<Long> subscriber = new Subscriber<Long>() {
            @Override
            public void onCompleted() {
                ConcurrencyUtil.log("DONE");
            }

            @Override
            public void onError(Throwable e) {
                ConcurrencyUtil.log("ERR");
            }

            @Override
            public void onNext(Long aLong) {
                ConcurrencyUtil.log("elem " + aLong);
            }
        };
        Observable.interval(500, TimeUnit.MILLISECONDS)
            .map(n -> {
                ConcurrencyUtil.log("Square " + n);
                return n * n;
            })
            .subscribe(subscriber);
        ConcurrencyUtil.sleep(1600);


        subscriber.unsubscribe(); // TODO switch to Observer and try this again
        ConcurrencyUtil.log("Unsubscribed");

        ConcurrencyUtil.sleep(1000);
        ConcurrencyUtil.log("END");
    }
}
