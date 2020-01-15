package victor.training.rx.pitfall;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import victor.training.rx.ConcurrencyUtil;

@Slf4j
public class ReplayVsCache {
    public static void main(String[] args) {
        // ~~~ REF "getFavPickupPoint"
       Observable<String> request = Observable.defer(() -> Observable.just(httpSyncCall()))
               // TODO play
                .cache();
//                .replay(); // autoConnect
        log.debug("Cached the obs. Subscribing");
        request.subscribe(s -> log.debug("Using " + s));
        request.subscribe(s -> log.debug("Using " + s));
        ConcurrencyUtil.sleep(2000);
        log.debug("A late subscriber");
        request.subscribe(s -> log.debug("Using " + s));

//        request.connect();
    }

    private static String httpSyncCall() {
        log.debug("Sent HTTP Request. Waiting for response");
        ConcurrencyUtil.sleep(1000);
        log.debug("Got Response.");
        return "response";
    }
}
