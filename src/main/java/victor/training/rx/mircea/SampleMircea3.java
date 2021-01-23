package victor.training.rx.mircea;

import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import victor.training.rx.sampleOPP2.LocationId;
import victor.training.rx.sampleOPP2.PickingJobInfoRepository;
import victor.training.rx.sampleOPP2.ProposalService;
import victor.training.rx.sampleOPP2.TourId;

public class SampleMircea3 {
   private static final Logger LOGGER = LoggerFactory.getLogger(SampleMircea3.class);
   private PickingJobInfoRepository pickingJobInfoRepository;
   private ProposalService proposalService;
   private Scheduler scheduler = Schedulers.io();

   private Completable triggerProposalUpdatesAfterAssignment(SingleSlotTrackingUpdated slotTrackingUpdated, String zoneNameFromSlotId, OppFlexibleWaveDetails config) {
      LOGGER.debug("Created asynchronous triggerProposalUpdatesAfterAssignment in mtp inactive mode for tourId {}, proposedZoneName {}", slotTrackingUpdated.getTourId(), zoneNameFromSlotId);

      pickingJobInfoRepository.getPickingJobInfosForLocation(LocationId.fromString(slotTrackingUpdated.getLocationId()))
          .filter(pji -> hasSameProposedZone(zoneNameFromSlotId, pji.getProposedZoneName()) || hasSameTourId(slotTrackingUpdated.getTourId(), pji.getTourId()))
          .filter(pji -> !config.isMopActive(pji.getAssortmentArea()))
          .groupBy(this::locationIdAreaTourIdTuple, PickingJobInfo::getPickingJobId)
          .doOnNext(locationIdAreaTourIdTuple -> LOGGER.debug("Started asynchronous triggerProposalUpdatesAfterAssignment for tourId {}, proposedZoneName {}, ARGS {}, {}, {}, {}",
              slotTrackingUpdated.getTourId(), zoneNameFromSlotId,
              locationIdAreaTourIdTuple.getKey().getT1(),
              locationIdAreaTourIdTuple.getKey().getT2(),
              locationIdAreaTourIdTuple.getKey().getT3().asString()))
          .concatMap(groupedPickingJobInfo -> proposalService.calculateAndPublishProposal(
              groupedPickingJobInfo.getKey().getT1(),
              groupedPickingJobInfo.getKey().getT2(),
              groupedPickingJobInfo.getKey().getT3().asString(),
              groupedPickingJobInfo.asObservable()) // TODO victor de ce ?
              .doOnError(e -> LOGGER.error("asynchronous triggerProposalUpdatesAfterAssignment failed for picking job group '{}'", groupedPickingJobInfo.getKey(), e))
              .onErrorResumeNext(Observable.empty())
              .doOnCompleted(() -> LOGGER.info("Finished asynchronous triggerProposalUpdatesAfterAssignment for tourId {}, proposedZoneName {}",
                     slotTrackingUpdated.getTourId(), zoneNameFromSlotId)))
          .subscribeOn(scheduler)
          .subscribe(   // TODO victor ?!!! Why do you subscribe programatically?
              next -> { /* ignore */ },
              error -> LOGGER.error("Error during asynchronous triggerProposalUpdatesAfterAssignment", error),
              () -> LOGGER.debug("Subscribed asynchronous triggerProposalUpdatesAfterAssignment")
          );

      return Completable.complete();
   }

   private Tuple3<LocationId, String, TourId> locationIdAreaTourIdTuple(PickingJobInfo pickingJobInfo) {
      return null;
   }

   private Boolean hasSameProposedZone(String zoneNameFromSlotId, String proposedZoneName) {
      return null;
   }

   private boolean hasSameTourId(TourId tourId, TourId tourId1) {
      return false;
   }
}
