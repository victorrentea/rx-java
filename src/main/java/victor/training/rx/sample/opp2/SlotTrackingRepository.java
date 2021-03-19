package victor.training.rx.sample.opp2;

import rx.Observable;
import rx.Single;
import victor.training.rx.sample.mircea.SingleSlotTrackingUpdated;

import java.util.List;

public class SlotTrackingRepository {
   public Observable<Object> getSlotTrackingsEntriesForSSCCList(LocationId locationId, List<String> value) {
      return null;
   }

   public Single<SingleSlotTrackingUpdated> insertSSCCs(SingleSlotTrackingUpdated singleSlotTrackingUpdated) {
      return Single.just(singleSlotTrackingUpdated);
   }
}
