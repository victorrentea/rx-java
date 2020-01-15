package victor.training.rx.exercise;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

@Slf4j
public class IrregularEmmit {
    public static void main(String[] args) {
        List<Integer> delays = asList(100, 100, 200, 300, 100, 200);
        emitAtIntervals(delays).subscribe(tick -> log.debug("tick"));
    }

    public static Observable<Integer> emitAtIntervals(Integer ...delays) {
        return emitAtIntervals(asList(delays));
    }
    public static Observable<Integer> emitAtIntervals(List<Integer> delays) {
        return Observable.from(delays);
                // TODO emit 0,1,2,3,... with the delays in between given as params
    }
}
