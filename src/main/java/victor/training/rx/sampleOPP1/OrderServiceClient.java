package victor.training.rx.sampleOPP1;

import com.google.common.collect.ImmutableList;
import rx.Observable;
import rx.Single;

public class OrderServiceClient {
   public Single<ImmutableList<PickListWithOrderData>> getPicklistsForOrderId(Long orderId, BettyRequestContext requestContext) {
      return null;
   }
}
