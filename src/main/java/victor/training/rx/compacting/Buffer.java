package victor.training.rx.compacting;

import rx.Observable;
import victor.training.rx.IrregularEmmit;
import victor.training.rx.ConcurrencyUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public class Buffer {
    public static void main(String[] args) {

        IrregularEmmit.emitAtIntervals(asList(45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45))
                .map(tick -> {
//                    System.out.println("Creez");
                    return "comanda#"+tick;
                })
//                .buffer(300, TimeUnit.MILLISECONDS)
//                .buffer(5)
//                .delay
                .buffer(100, TimeUnit.MILLISECONDS,2)
                .flatMap(Buffer::doSearchOverHttp)
                .subscribe(r -> System.out.println(r));


        ConcurrencyUtil.sleep(3000);
    }

    public static Observable<String> doSearchOverHttp(List<String> comenzi) {
        return Observable.just("plasez comenzi: " + comenzi)
                .delay(100, TimeUnit.MILLISECONDS);
    }
}
