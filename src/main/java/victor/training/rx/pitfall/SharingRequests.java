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
        log.debug("Start");

        System.out.println("===ConnectableObserver===");
        Observable<String> call = getFromHttp();
        ConnectableObservable<String> birouDeschis = call.publish();
        // TODO avoid doing two expensive network calls
        birouDeschis.subscribe(v -> System.out.println("Pun in cas: " + v));
        birouDeschis.subscribe(v-> System.out.println("Dau aplicatiei"));
        birouDeschis.connect(); // abia acum observable-ul incepe sa emita
        birouDeschis.subscribe(v-> System.out.println("Dau aplicatiei 3 = NU RULEAZA"));

        System.out.println("===autoConnect===");
        Observable<String> pornesteAutomatCandAre2 = getFromHttp().publish().autoConnect(2);
        pornesteAutomatCandAre2.subscribe(v -> System.out.println("Pun in cas: " + v));
        pornesteAutomatCandAre2.subscribe(v -> System.out.println("Dau aplicatiei"));
        pornesteAutomatCandAre2.subscribe(v -> System.out.println("Dau aplicatiei 3  = NU RULEAZA"));

        System.out.println("===encapsulatedCache:chainObservers===");
        // PROBLEM: launches the first http request before .subscribe is called here.
        getFromHttpOrCache().subscribe(System.out::println);

        System.out.println("===.defer to fix above===");
        Observable.defer(() -> getFromHttpOrCache());// correct ugly solution


        System.out.println("===Time Observable that doesn't restart on subscribe===");
        Observable<Long> timp = Observable.interval(100, TimeUnit.MILLISECONDS).share();
        timp.subscribe(System.out::println);
        ConcurrencyUtil.sleep(1000);
        timp.subscribe(System.out::println);
        ConcurrencyUtil.sleep(2000);
    }

    private static Observable<String> getFromHttpOrCache() {
        // PROBLEMA: iti face HTTP req chiar daca observableul intors nu e subscris de nimeni.
        BehaviorSubject<String> subject = BehaviorSubject.create();
        getFromHttp()
                .subscribe(v -> {
                    System.out.println("punInCache " + v);
                    subject.onNext(v);
                });
        return subject.asObservable();

        // parca tot mai bun e asta:
//        return httpCallObs().doOnNext(v -> System.out.println("punInCache " + v));

    }

    private static Observable<String> getFromHttp() {
        return Observable.fromCallable(() -> {
            log.debug("Sent HTTP Request. Waiting for response");
            ConcurrencyUtil.sleep(1000);
            log.debug("Got Response.");
            return "response";
        });
    }
}
