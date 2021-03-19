package victor.training.rx.sample.mircea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import victor.training.rx.sample.opp2.LocationId;
import victor.training.rx.sample.opp2.PickingJobInfoRepository;
import victor.training.rx.sample.opp2.ProposalService;
import victor.training.rx.sample.opp2.TourId;

public class SampleMircea3 {
   private static final Logger LOGGER = LoggerFactory.getLogger(SampleMircea3.class);
   private PickingJobInfoRepository pickingJobInfoRepository;
   private ProposalService proposalService;
   private Scheduler scheduler = Schedulers.io();

   private Completable triggerProposalUpdatesAfterAssignment(SingleSlotTrackingUpdated slotTrackingUpdated, String zoneNameFromSlotId, OppFlexibleWaveDetails config) {
      LOGGER.debug("Created asynchronous triggerProposalUpdatesAfterAssignment in mtp inactive mode for tourId {}, proposedZoneName {}", slotTrackingUpdated.getTourId(), zoneNameFromSlotId);

      pickingJobInfoRepository.getPickingJobInfosForLocation(LocationId.fromString(slotTrackingUpdated.getLocationId()))
          .filter(pji -> canProcess(slotTrackingUpdated, zoneNameFromSlotId, config, pji))

          .groupBy(this::locationIdAreaTourIdTuple, PickingJobInfo::getPickingJobId)
//          .doOnNext(locationIdAreaTourIdTuple -> logLocationArea(slotTrackingUpdated, zoneNameFromSlotId, locationIdAreaTourIdTuple))

          .concatMap(groupedPickingJobInfo -> proposalService.calculateAndPublishProposal(
              groupedPickingJobInfo.getKey().getT1(),
              groupedPickingJobInfo.getKey().getT2(),
              groupedPickingJobInfo.getKey().getT3().asString(),
              groupedPickingJobInfo)
              .onErrorResumeNext(Observable.empty()))
          .subscribeOn(scheduler)
          .subscribe(   // TODO victor ?!!! Why do you subscribe programatically?
              next -> { /* ignore */ },
              error -> LOGGER.error("Error during asynchronous job triggerProposalUpdatesAfterAssignment", error),
              () -> LOGGER.debug("Completed the  asynchronous job triggerProposalUpdatesAfterAssignment")
          );

      return Completable.complete();
   }

   private boolean canProcess(SingleSlotTrackingUpdated slotTrackingUpdated, String zoneNameFromSlotId, OppFlexibleWaveDetails config, PickingJobInfo pji) {
      return (hasSameProposedZone(zoneNameFromSlotId, pji.getProposedZoneName()) || hasSameTourId(slotTrackingUpdated.getTourId(), pji.getTourId()))
             && !config.isMopActive(pji.getAssortmentArea());
   }

   private void logLocationArea(SingleSlotTrackingUpdated slotTrackingUpdated, String zoneNameFromSlotId, rx.observables.GroupedObservable<Tuple3<LocationId, String, TourId>, String> locationIdAreaTourIdTuple) {
      LOGGER.debug("Started asynchronous triggerProposalUpdatesAfterAssignment for tourId {}, proposedZoneName {}, ARGS {}, {}, {}, {}",
          slotTrackingUpdated.getTourId(), zoneNameFromSlotId,
          locationIdAreaTourIdTuple.getKey().getT1(),
          locationIdAreaTourIdTuple.getKey().getT2(),
          locationIdAreaTourIdTuple.getKey().getT3().asString());
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
