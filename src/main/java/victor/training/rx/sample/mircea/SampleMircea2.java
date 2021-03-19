package victor.training.rx.sample.mircea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import victor.training.rx.sample.opp2.*;

public class SampleMircea2
{
   private static final Logger LOGGER = LoggerFactory.getLogger(SampleMircea2.class);
   private SSCC2OrderIdService sscc2OrderIdService;
   private OrderInfoRepository orderInfoRepository;
   private SlotTrackingRepository slotTrackingRepository;
   private SSCCSlotAssignedProducer ssccSlotAssignedProducer;

   public Completable saveSlotTracking(SlotTrackingUpdated slotTrackingUpdated) {
      LOGGER.info("saveSlotTracking invoked for locationId {}, slotid {} and ssccs {}", slotTrackingUpdated.getLocationId(), slotTrackingUpdated.getSlotId(), slotTrackingUpdated.getSsccs());

      // fetch orderIds based on the SSCCs at once
      LocationId locationId = LocationId.fromString(slotTrackingUpdated.getLocationId());
      TourId tourId = TourId.fromString(slotTrackingUpdated.getTourId());

      return sscc2OrderIdService.getSscc2OrderIdEntriesBySsccList(locationId, tourId, slotTrackingUpdated.getSsccs())
          .toMultimap(Sscc2OrderIdEntry::getOrderId, Sscc2OrderIdEntry::getSscc)
          // one item with a map here
          .flatMap(orderToSsccsMap ->
              orderInfoRepository.getOrderInfoEntriesByLocationAndOrderIdsList(slotTrackingUpdated.getLocationId(), orderToSsccsMap.keySet())
                  .map(o ->  /*new OrderWithSSCs*/o.setAllSSCC(orderToSsccsMap.get(o.getOrderId()))))
          // many items here
          .flatMap(orderInfoDetail -> Observable.from(orderInfoDetail.getSSCCs())
              .map(sscc -> createSingleSlotTrackingUpdated(slotTrackingUpdated, orderInfoDetail, sscc)))
          // save each SingleSlotTrackingUpdated
          .flatMap(singleSlotTrackingUpdated -> slotTrackingRepository.insertSSCCs(singleSlotTrackingUpdated)
              .flatMap(record -> ssccSlotAssignedProducer.publishSingleSlotTrackingUpdated(record))
              .toObservable())
          .cache()
          .first() // ?
          .flatMapCompletable(this::triggerProposalUpdatesAfterAssignment)
          .toCompletable()
          .doOnError(e -> LOGGER.error("An error occurred during the saveSlotTracking for slotTrackingUpdated (Tour {} SlotId {} SSCCs {}): {}",
              slotTrackingUpdated.getTourId(), slotTrackingUpdated.getSlotId(), slotTrackingUpdated.getSsccs(), e.getMessage(), e))
          .onErrorResumeNext(e -> Completable.error(new BadRequestException("Could not save the SlotTracking.", e)));

   }

   private Completable triggerProposalUpdatesAfterAssignment(Object o) {
      return null;
   }

   private SingleSlotTrackingUpdated createSingleSlotTrackingUpdated(SlotTrackingUpdated slotTrackingUpdated, OrderInfoDetail orderInfoDetail, String sscc) {
      return null;
   }
}
