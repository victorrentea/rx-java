package victor.training.rx;


import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class FlatMapZip {
    public static void main(String[] args) {
        log.debug("Start");
        Observable<Long> movieIdObs = Observable.just(13L, 14L, 15L);

        // TODO for each ID, call requestPlot and requestRating, zip the results and println them

        // TODO zip->flatMap vs flatMap->zip
    }

    public static Observable<Float> requestRating(long movieId) {
        return Observable.just(3.5f).delay(1, SECONDS);
    }
    public static Observable<String> requestPlot(long movieId) {
        return Observable.just("mare").delay(2, SECONDS);
    }
}

@Value
class PlotAndRating {
    String plot;
    float rating;
}