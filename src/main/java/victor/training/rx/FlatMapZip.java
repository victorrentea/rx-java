package victor.training.rx;


import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.concurrent.TimeUnit;

@Slf4j
public class FlatMapZip {
    public static void main(String[] args) {

        log.debug("Start");

        // useru face un click pt filmul 13
        Observable<Long> movieIdObs = Observable.just(13L);
        Observable<String> plotObs = movieIdObs.flatMap(FlatMapZip::requestPlot);
        Observable<Float> ratingOps = movieIdObs.flatMap(FlatMapZip::requestRating);

        Observable.zip(plotObs, ratingOps, PlotAndRating::new)
                .subscribe(FlatMapZip::display);

//        movieIdObs.flatMap(id ->
//                Observable.zip(requestPlot(id), requestRating(id), PlotAndRating::new))
//                .subscribe(pr -> display(pr.getPlot(), pr.getRating()));

        ConcurrencyUtil.sleep(3000);
    }

    public static void display(PlotAndRating pr ) {
        System.out.println("Movie " + pr.getPlot() + " and " + pr.getRating());
    }

    public static Observable<Float> requestRating(long movieId) {
        return Observable.just(3.5f)
                    .delay(1, TimeUnit.SECONDS);
    }
    public static Observable<String> requestPlot(long movieId) {
        return Observable.just("mare")
                    .delay(2, TimeUnit.SECONDS);
    }
}


@Value
class PlotAndRating {
    String plot;
    float rating;
}