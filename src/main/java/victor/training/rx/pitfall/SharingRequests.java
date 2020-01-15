package victor.training.rx.pitfall;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.subjects.BehaviorSubject;
import victor.training.rx.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;

@Slf4j
public class SharingRequests {
    public static void main(String[] args) {
        // TODO avoid doing two expensive network calls

        System.out.println("===Re-Subscribe :(===");
        Observable<String> call = getFromHttp();
        call.subscribe(v -> System.out.println("Store in cache: " + v));
        call.subscribe(v -> System.out.println("Hand over to app"));

        System.out.println("===ConnectableObserver===");

        System.out.println("===autoConnect===");

        System.out.println("===encapsulatedCache:chainObservers===");
        // PROBLEM: launches the first http request before .subscribe is called here.
        getFromHttpOrCache().subscribe(System.out::println);

        System.out.println("===.defer to fix above===");


        System.out.println("===.interval Observable that doesn't restart on re-subscribe===");
        Observable<Long> time = Observable.interval(100, TimeUnit.MILLISECONDS);
        time.subscribe(System.out::println);
        ConcurrencyUtil.sleep(1000);
    }

    private static Observable<String> getFromHttpOrCache() {
        BehaviorSubject<String> subject = BehaviorSubject.create();
        // TODO chain Observable
        // TODO onNext
        return subject.asObservable();
    }

    private static Observable<String> getFromHttp() {
        return Observable.fromCallable(() -> {
            log.debug("Sent HTTP Request. Waiting for response");
            ConcurrencyUtil.sleep(1000);
            log.debug("Got Response");
            return "response";
        });
    }
}
