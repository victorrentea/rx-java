package victor.training.rx.sample.opp1;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import rx.Observable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
// lavinia.soroiu
public class SampleOPP1 {
   private OrderServiceClient orderServiceClient;
   private OPPFeatures oppFeatures;
   private ArticleDataService articleDataService;

   private Observable<PickAndPackPicklist> obtainPicklistForOrder(PickAndPackOrderHeader order, BettyRequestContext requestContext) {
      return orderServiceClient.getPicklistsForOrderId(order.getOrderId(), requestContext)
          // return an empty list so that no events are created but the OrderUpdated events gets committed
          .onErrorReturn(e -> ImmutableList.of())
          .map(pickLists -> Lists.transform(pickLists, pl -> new ImmutablePair<>(pl, pickLists)))
          .flatMapObservable(Observable::from)
          .map(picklistPair -> PicklistTranslator.joinOrderWithPicklist(order,
              picklistPair.getLeft(),
              picklistPair.getRight(),
               oppFeatures.getStoreLocations()))
          .flatMap(picklist -> enhanceWithReferencedEansIfStoreLocation(picklist))
          .doOnError(error -> warnOnError(order, error))
          .onErrorResumeNext(e -> Observable.empty());
   }

   private void warnOnError(PickAndPackOrderHeader order, Throwable error) {

   }


   private Observable<PickAndPackPicklist> enhanceWithReferencedEansIfStoreLocation(PickAndPackPicklist picklist) {
      log.debug("Picklist obtained {}", picklist);
      if (LocationType.STORE.equals(picklist.getLocationType())) {
         return enhanceWithReferencedItemsEanData(picklist);
      }
      return Observable.just(picklist);
   }

   private Observable<PickAndPackPicklist> enhanceWithReferencedItemsEanData(PickAndPackPicklist picklist) {
      return articleDataService
          .getEanBundleMapForReferencedArticles(picklist.getLocationId(), picklist.getPicklistId(),
              extractReferencedBundleIdsFromItems(picklist.getItems()))
          .map(bundleToEanMap -> setEanToBundleForPicklistItems(bundleToEanMap, picklist))
          .toObservable();
   }

   private List<String> extractReferencedBundleIdsFromItems(List<PickAndPackItem> pickListItems) {
      return pickListItems.stream()
          .flatMap(picklistItem -> picklistItem.getReferencedBundleIds().stream())
          .collect(Collectors.toList());
   }

   private PickAndPackPicklist setEanToBundleForPicklistItems(Map<String, String> eanToBundleIdMapForPicklist, PickAndPackPicklist picklist) {
      if (!eanToBundleIdMapForPicklist.isEmpty()) {
         final List<PickAndPackItem> updatedPicklistItems = picklist.getItems().stream()
             .map(picklistItem -> buildUpdatedPicklistItem(eanToBundleIdMapForPicklist, picklistItem))
             .collect(Collectors.toList());
         return buildUpdatedPicklist(picklist, updatedPicklistItems);
      }
      return picklist;
   }

   private PickAndPackPicklist buildUpdatedPicklist(PickAndPackPicklist picklist, List<PickAndPackItem> updatedPicklistItems) {
      return null;
   }

   private PickAndPackItem buildUpdatedPicklistItem(Map<String, String> eanToBundleIdMapForPicklist, PickAndPackItem picklistItem) {
      return null;
   }

}
