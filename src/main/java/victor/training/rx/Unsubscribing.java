package victor.training.rx;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

import java.util.concurrent.TimeUnit;

public class Unsubscribing {

    public static void main(String[] args) {
        Observable<Long> o1 = Observable.interval(100, TimeUnit.MILLISECONDS);
        Observable<Long> o2 = Observable.interval(200, TimeUnit.MILLISECONDS);

        o1.subscribe(System.out::println);
        o2.subscribe(System.out::println);

        ConcurrencyUtil.sleep(1000);
        System.out.println("Closing the page");
        // TODO unsubscribe from **all**
        // Hint: Composite...   .clear or .unsubscribe ?

        ConcurrencyUtil.sleep(1000);
        System.out.println("Entering again");

        ConcurrencyUtil.sleep(1000);
        System.out.println("Closing the page #2");

        ConcurrencyUtil.sleep(1000);
    }
}
