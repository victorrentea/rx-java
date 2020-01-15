package victor.training.rx.exercise;

import rx.Emitter;
import rx.Observable;
import rx.schedulers.Schedulers;
import victor.training.rx.ConcurrencyUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static victor.training.rx.ConcurrencyUtil.log;

public class BackPressure {

    static Consumer<Long> task;

    public static void main(String[] args) {


        new Thread(() -> {
            while(true) {
                long v = System.currentTimeMillis();
                ConcurrencyUtil.log("Gen " + v);
                ConcurrencyUtil.sleep(50);
                if (task != null) {
                    task.accept(v);
                }
            }
        }).start();


        Observable<Long> observable = Observable.create(integerEmitter -> {
            task = aLong -> integerEmitter.onNext(aLong);
        }, Emitter.BackpressureMode.ERROR);

        // TODO subscribe to this observer from another thread and consume items slower
        ExecutorService pool = Executors.newFixedThreadPool(2);
        observable
                .observeOn(Schedulers.from(pool))
                .subscribe(x -> {
                    ConcurrencyUtil.sleep(200);
                    ConcurrencyUtil.log("Got " + x);
                });
        ConcurrencyUtil.sleep(2000);
    }


}
