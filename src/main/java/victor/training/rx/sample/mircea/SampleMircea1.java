package victor.training.rx.sample.mircea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import rx.Single;
import victor.training.rx.sample.opp2.LocationId;
import victor.training.rx.sample.opp2.TourId;

public class SampleMircea1 {
   private static final Logger LOGGER = LoggerFactory.getLogger(SampleMircea1.class);
    private Completable processPickingJobInfo(Tourswitch tourswitch) { // Mono<Void>
       return Completable.defer(() -> // TODO victor de ce e necesar defer daca find returneaza  Observable?
           // TODO victor push reactivity down
       {
          final Observable<PickingJobInfo> getPicklingJobInfo = findPickingJobInfoByPicklistId(
              TourId.fromString(tourswitch.getDeletionCandidate().getOldTourId()),
              LocationId.fromString(tourswitch.getDeletionCandidate().getLocationId()),
              tourswitch.getDeletionCandidate().getPicklistId());

          return getPicklingJobInfo
              .concatMap(pickingJobInfo -> removePicklistFromPickingJoInfo(tourswitch, pickingJobInfo))
              .concatMap(pickingJobInfos -> manageNewTour(tourswitch, pickingJobInfos))
              .toCompletable()
              .doOnError(e -> LOGGER.warn("Error processing pickingjobs on tourswitch event.", e));
       });
    }

   public static void main(String[] args) {
      System.out.println(Single.just(null).toBlocking().value());
   }
   private Observable<?> manageNewTour(Tourswitch tourswitch, Object pickingJobInfos) {
       // TODO victor network call ? (i hope)
      return null;
   }

   private Observable<?> removePicklistFromPickingJoInfo(Tourswitch tourswitch, PickingJobInfo pickingJobInfo) {
       // TODO victor network call ? (i hope)

      Single<String> singleStuff = Single.just("");
      return singleStuff.toObservable().toSingle().toObservable();// webClient.bodyAsMono();
   }

   private Observable<PickingJobInfo> findPickingJobInfoByPicklistId(TourId fromString, LocationId fromString1, Long picklistId) {
       return null;
    }

}
