package victor.training.rx.sample.opp2;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;

// lavinia.soroiu
public class SampleOPP2

{
   private static final Logger LOGGER = LoggerFactory.getLogger(SampleOPP2.class);
   private TourInfoRepository tourInfoRepository;
   private SSCC2OrderIdService sscc2OrderIdService;
   private SlotTrackingRepository slotTrackingRepository;
   private PickingJobInfoRepository pickingJobInfoRepository;
   private ProposalService proposalService;

   public Single<SlotTrackingsMTPProposal> collectMTPInitData(LocationId locationId, String pickingJobId, Map<String, List<String>> tourToSsccs) {
      LOGGER.info("collectMTPInitData called with locationId {}, pickingJobId {}, ssccs {}.", locationId, pickingJobId, tourToSsccs);

      // calculate a single of list of proposals, each one based on the last pickingJobMTPInfo for every tour to pickingJobMTPId
      final Single<List<TargetZoneProposal>> targetZoneProposalList = getTargetZoneProposalList(locationId, pickingJobId, tourToSsccs);

      // existing slottracking lists - is probably empty, since this may be the first call even before a slot had been scanned...
      final Single<List<SlotTrackingListReduced>> slotTrackingListReducedList = getSlotTrackingListReducedList(locationId, tourToSsccs);

      // maps the routeName to its startTime
      final Single<Map<String, String>> routeNameToStartTimeMap = getRouteNameToStartTimeMap(locationId, pickingJobId, tourToSsccs);

      // maps the order to its assigned tour and all the designated ssccs.
      final Single<Map<String, ImmutablePair<String, List<String>>>> orderToTourToSsccs = getOrderToTourToSsccs(locationId, tourToSsccs);

      // maps each zoneName to orders

//      Single<I1>
//          .flatMap(I2) :Single<Tuple<I1,I2>>
//          .flatMap(I3) : STuple<I1,I2,I3>

      return Single.zip(
          slotTrackingListReducedList,
          targetZoneProposalList,
          routeNameToStartTimeMap,
          orderToTourToSsccs,

          (slotTrackingListReducedListData,
           targetZoneProposalListData,
           routeNameToStartTimeMapData,
           orderToTourToSsccsData) -> {

             final Single<TreeMap<String, List<String>>> zoneNameToOrderIdsAsSingle2 =
                 getZoneNameToOrderIds(targetZoneProposalListData, Single.just(orderToTourToSsccsData)  ); // TODO give up using Single at all in the #getZoneNameToOrderIds method


             return new SlotTrackingsMTPProposal(slotTrackingListReducedListData,
                 targetZoneProposalListData, routeNameToStartTimeMapData, orderToTourToSsccsData,


                 zoneNameToOrderIdsAsSingle2.toBlocking().value()); // doesn't really block anything here !
          })




          .doOnSuccess(slotTrackingsProposal -> LOGGER.debug("Slot tracking proposal has been successfully zipped {}", slotTrackingsProposal))
          .doOnError(error -> LOGGER.error("An error occurred during the collection of init mtp data for {} {}, with error {}", locationId, pickingJobId, error.getMessage(), error))
          .onErrorResumeNext(error -> {
             if (error instanceof NoSuchElementException) {
                return Single.error(new NotFoundException("Could not create a SlotTrackingsMTPProposal "
                                                          + "because no PickingJobMTPInfo entries could be found for location '" + locationId + "' and pickingJobId '" + pickingJobId + "'", error));
             } else {
                return Single.error(new BadRequestException("An unexpected error occurred.", error));
             }
          });
   }
      int i= 1;

   public void method(boolean b) {

   i= 6;
      if (b) {
         i = 2;
      }
      i =3;

      f();
   }

   private void f() {
//      System.out.println(i);
   }


   //=================
   private Single<List<TargetZoneProposal>> getTargetZoneProposalList(LocationId locationId, String pickingJobId, Map<String, List<String>> tourToSsccs) {
      return Observable.from(tourToSsccs.keySet())
          .concatMap(tourId -> pickingJobInfoRepository.getPickingJobMTPInfoEntriesFor(locationId.asString(), tourId, pickingJobId)
              .last()
              .doOnError(error -> LOGGER.error("PickingJobMTPInfo is missing for the given location {}, tour {}, id {}", locationId, tourId, pickingJobId, error))
              .flatMap(pickingJobMTPInfo -> proposalService.calculate(locationId,
                  Optional.ofNullable(pickingJobMTPInfo.getAssortmentArea()).map(AssortmentArea::forValue).orElse(AssortmentArea.UNDEFINED),
                  tourId)
                  .toObservable()
                  .map(TargetZoneProposal::fromResult)
                  .doOnCompleted(() -> LOGGER.info("Target zone proposal has been calculated for location {} and tour id {}",
                      locationId, pickingJobMTPInfo.getTourId()))
                  .doOnError(error -> LOGGER.error("An error occurred at proposal service calculation {}", error.getMessage(), error))))
          .toList()
          .toSingle()
//          .cache() tricky to understand
          ;
   }

   private Single<List<SlotTrackingListReduced>> getSlotTrackingListReducedList(LocationId locationId, Map<String, List<String>> tourToSsccs) {
      return Observable.from(tourToSsccs.entrySet())
          .concatMap(tourToSsccsEntry -> slotTrackingRepository.getSlotTrackingsEntriesForSSCCList(locationId, tourToSsccsEntry.getValue())
              .toList()
              .map(SlotTrackingListReduced::create)
              .doOnCompleted(() -> LOGGER.info("Assigned to location {} and ssccs {} for tour {}",
                  locationId, tourToSsccsEntry.getValue(), tourToSsccsEntry.getKey()))
              .doOnError(error -> LOGGER.error("Could not find a slotTracking entry for location {} ssccs {}, error {}",
                  locationId, tourToSsccsEntry.getValue(), error.getMessage(), error))
              .onErrorReturn(e -> new SlotTrackingListReduced()))
          .toList()
          .toSingle();
   }

   private Single<Map<String, String>> getRouteNameToStartTimeMap(LocationId locationId, String pickingJobId, Map<String, List<String>> tourToSsccs) {
      return Observable.from(tourToSsccs.keySet())
          .concatMap(tourId -> tourInfoRepository.getTourByPK(locationId, TourId.fromString(tourId))
              .toObservable()
              .last()
              .doOnCompleted(() -> LOGGER.info("Tour info with tour id {} has been found for location {} and pickingJobId {}",
                  tourId, locationId, pickingJobId))
              .doOnError(error -> LOGGER.error("An error occurred when retrieving the tour info for location id {} and picking job id {}, {}",
                  locationId, pickingJobId, error.getMessage(), error)))
          .map(tourInfo -> new ImmutablePair<>(tourInfo.getRouteName(), format(tourInfo.getTourDepartureTime())))
          .toMap(Pair::getKey, Pair::getValue)
//          .subscribeOn(Schedulers.io())
          .toSingle();
   }

   private String format(LocalDateTime tourDepartureTime) {
      return null;
   }


//   tourToSsccsEntry ->
//    sscc2OrderIdService.getSscc2OrderIdEntriesBySsccList(locationId, tourToSsccsEntry.getValue())
//    .reduce(new HashMap<>(), this::groupByOrderId)

   private Single<Map<String, ImmutablePair<String, List<String>>>> getOrderToTourToSsccs(LocationId locationId, Map<String, List<String>> tourToSsccs) {



      return Observable.from(tourToSsccs.entrySet())
          .concatMap(tourToSsccsEntry ->
                 sscc2OrderIdService.getSscc2OrderIdEntriesBySsccList(locationId, tourToSsccsEntry.getValue())
                  .reduce(new HashMap<>(), this::groupByOrderId)
          )
          .reduce(this::mergeOrderToTourToSsccsMaps)
          .doOnCompleted(() -> LOGGER.info("Order to tour to ssccs has been generated successfully for tour to ssccs map {}", tourToSsccs))
          .doOnError(error -> LOGGER.error("Error has occurred when generating order to tour to sccss {}", error.getMessage(), error))
          .toSingle();
   }

   private Map<String, ImmutablePair<String, List<String>>> groupByOrderId(Map<String, ImmutablePair<String, List<String>>> stringImmutablePairMap, String s) {
      return null;
   }

   private Map<String, ImmutablePair<String, List<String>>> mergeOrderToTourToSsccsMaps(Map<String, ImmutablePair<String, List<String>>> accumulatorOrderToTourToSsccsMap,
                                                                                        Map<String, ImmutablePair<String, List<String>>> nextOrderToTourToSsccsMap) {
      accumulatorOrderToTourToSsccsMap.putAll(nextOrderToTourToSsccsMap);
      return accumulatorOrderToTourToSsccsMap;
   }

   private Single<TreeMap<String, List<String>>> getZoneNameToOrderIds(List<TargetZoneProposal> targetZoneProposalList,
                                                                       Single<Map<String, ImmutablePair<String, List<String>>>> orderToTourToSsccs) {
      return orderToTourToSsccs.toObservable()
          .flatMap(orderToTourToSsccsMap -> Observable.from(orderToTourToSsccsMap.entrySet()))

          .map(orderToTourEntry -> groupZoneNameToOrderIds(
              targetZoneProposalList,
              orderToTourEntry.getValue().getLeft(),
              orderToTourEntry.getKey()))


          .reduce(this::mergeZoneNameToOrderIds)
          .toSingle();
   }

   private TreeMap<String, List<String>> groupZoneNameToOrderIds(List<TargetZoneProposal> targetZoneProposalList,
                                                                         String tourId, String order) {
      final TreeMap<String, List<String>> zoneNameToOrderIds = new TreeMap<>();

      TargetZoneProposal targetZoneProposal = targetZoneProposalList.stream()
          .filter(tt -> StringUtils.equals(tt.getTourId(), tourId))
          .findFirst()
          .get();

       if (zoneNameToOrderIds.containsKey(targetZoneProposal.getZoneName())) {
          zoneNameToOrderIds.get(targetZoneProposal.getZoneName()).add(order);
       } else {
          List<String> orderIds = new ArrayList<>();
          orderIds.add(order);
          zoneNameToOrderIds.put(targetZoneProposal.getZoneName(), orderIds);
       }
       return zoneNameToOrderIds;
   }

   private TreeMap<String, List<String>> mergeZoneNameToOrderIds(TreeMap<String, List<String>> stringListTreeMap, TreeMap<String, List<String>> stringListTreeMap1) {
      return null;
   }


}
