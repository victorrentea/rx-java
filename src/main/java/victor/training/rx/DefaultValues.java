package victor.training.rx;


import rx.Observable;

import java.util.concurrent.TimeUnit;

public class DefaultValues {

    public static void main(String[] args) {
        getRecommendations()
                // TODO toilet paper is always a good recommendation
                // TODO get recommend from local storage
                .toBlocking() // for testing only
                .subscribe(System.out::println);
    }

    private static Observable<String> getRecommendations() { // pretend HTTP
        // n-am :)
        return Observable.empty();
    }

    public static Observable<String> getFromLocalStorage() {
        return Observable.just("Pencils", "Notebooks")
                .delay(1, TimeUnit.SECONDS);
    }
}
