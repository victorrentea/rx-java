package victor.training.rx;

import rx.Observable;
import rx.Observer;
import rx.observables.SyncOnSubscribe;

import java.util.Arrays;

import static rx.schedulers.Schedulers.computation;
import static rx.schedulers.Schedulers.io;
import static victor.training.rx.ConcurrencyUtil.log;

public class ThreadControl {

    public static void main(String[] args) {
        Observable.create(new SyncOnSubscribe<Long, Long>() {
            @Override
            protected Long generateState() {
                return 0L;
            }

            @Override
            protected Long next(Long state, Observer<? super Long> observer) {
                observer.onNext(state);
                ConcurrencyUtil.log("Computing next...");
                ConcurrencyUtil.sleep(200);
                return state + 2;
            }
        })
                .observeOn(computation())
                .subscribe(ConcurrencyUtil::log);



        Observable.defer(() -> getUsers())
                .map(s-> {
                    ConcurrencyUtil.log("map");
                    return s.toUpperCase();
                })
//                .subscribeOn(computation())
                .observeOn(computation())
                .doOnNext(s-> ConcurrencyUtil.log("map2 " +s))
                .observeOn(io())
                .subscribe(s -> ConcurrencyUtil.log(s));
        ConcurrencyUtil.log("Done");
        ConcurrencyUtil.sleep(100);
    }

    static Observable<String> getUsers() {
        ConcurrencyUtil.log("Create");
        return Observable.from(Arrays.asList("a","b","c"));
    }
}
