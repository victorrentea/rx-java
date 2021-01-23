package victor.training.rx.mircea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import victor.training.rx.sampleOPP2.*;

import javax.xml.stream.Location;

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
          .flatMap(orderToSsccsMap -> orderInfoRepository.getOrderInfoEntriesByLocationAndOrderIdsList(slotTrackingUpdated.getLocationId(), orderToSsccsMap.keySet())
              .map(o -> o.setAllSSCC(orderToSsccsMap.get(o.getOrderId()))))
          // iterate over
          .flatMap(orderInfoDetail -> Observable.from(orderInfoDetail.getSSCCs())
              .map(sscc -> createSingleSlotTrackingUpdated(slotTrackingUpdated, orderInfoDetail, sscc)))
          // save each SingleSlotTrackingUpdated
          .flatMap(singleSlotTrackingUpdated -> slotTrackingRepository.insertSSCCs(singleSlotTrackingUpdated)
              .toSingleDefault(singleSlotTrackingUpdated)
              .flatMap(record -> ssccSlotAssignedProducer.publishSingleSlotTrackingUpdated(record))
              .toObservable())
          .cache()
          .first() // TODO victor: adica vrei doar primul element publicat
          .flatMapCompletable(this::triggerProposalUpdatesAfterAssignment)
          .toCompletable() // TODO victor useless ?
          .doOnError(e -> LOGGER.error("An error occurred during the saveSlotTracking for slotTrackingUpdated (Tour {} SlotId {} SSCCs {}): {}",
              slotTrackingUpdated.getTourId(), slotTrackingUpdated.getSlotId(), slotTrackingUpdated.getSsccs(), e.getMessage(), e))
          .doOnError(e -> Completable.error(new BadRequestException("Could not save the SlotTracking.", e)));

   }

   private Completable triggerProposalUpdatesAfterAssignment(Object o) {
      return null;
   }

   private SingleSlotTrackingUpdated createSingleSlotTrackingUpdated(SlotTrackingUpdated slotTrackingUpdated, OrderInfoDetail orderInfoDetail, String sscc) {
      return null;
   }
}
