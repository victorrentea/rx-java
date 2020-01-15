package victor.training.rx.pitfall;

import rx.Observable;
import rx.exceptions.OnErrorNotImplementedException;
import rx.schedulers.Schedulers;
import victor.training.rx.ConcurrencyUtil;

public class OpaqueExceptionTrace {
    public static void main(String[] args) {

        // TODO find this code in the stacktrace; Make it appear
        Observable.empty()
                .first()
                .subscribeOn(Schedulers.io())
                .subscribe(System.out::println);

        ConcurrencyUtil.sleep(100);
    }
}
