package victor.training.rx.sample.madalina;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Madalina2 {

   private static final Logger LOGGER = LoggerFactory.getLogger(Madalina2.class);
   private static final String DD_ASPECT_UPSERT_TASKS_BY_KAFKA_EVENTS = "AA";
   private DataDogClient dataDogClient;

   private BatchAcknowledgingMessageListener<String, UpsertTasksEvent> processRecords(UpsertTaskInputTopic inputTopic) {

      return (consumerRecords, acknowledgment) -> {
         LocalDateTime startTime = LocalDateTime.now();
         try {
            LOGGER.debug("Number of clipboard tasks consumed events for lane {}: {}", inputTopic, consumerRecords.size());

            List<UpsertTasksEvent> upsertTasksEvents = consumerRecords.stream()
                .peek(consumerRecord -> LOGGER.info("inputTopic={}, partition={}, offset={}: Start tasks upserting process for event {}",
                    inputTopic, consumerRecord.partition(), consumerRecord.offset(), consumerRecord.value()))
                .map(ConsumerRecord::value)
                .collect(toList());

            consumerRecords.stream().findAny().ifPresent((ConsumerRecord<String, UpsertTasksEvent> r) ->
                dataDogClient.gauge(DD_ASPECT_UPSERT_TASKS_BY_KAFKA_EVENTS + inputTopic + ".partition-" + r.partition(), r.offset()));

            treatEvents(upsertTasksEvents, inputTopic);
            acknowledgment.acknowledge();
         } finally {
            dataDogClient.recordHistogramValue("ProcessKafkaBatchDuration",
                Duration.between(startTime, LocalDateTime.now()).toMillis(), "input_topic:" + inputTopic);
         }
      };
   }

   public void treatEvents(List<UpsertTasksEvent> upsertTasksEvents, UpsertTaskInputTopic inputTopic) {
      long numberOfTasksToUpsert = upsertTasksEvents.stream()
          .map(e -> e.getTasks())
          .flatMap(Collection::stream)
          .count();

      try {
         Observable.from(upsertTasksEvents)
             .flatMap(event -> Observable.just(event)
                 .subscribeOn(Schedulers.io())
                 .map(e -> {
                    upsertTasks(e, inputTopic);
                    return true;
                 }))
             .toBlocking()
             .subscribe();
      } catch (Exception e) {
         publishCountMetric(DD_ASPECT_UPSERT_TASKS_BY_KAFKA_EVENTS + inputTopic + ".TaskCountFailure", numberOfTasksToUpsert);
         throw new KafkaUpsertTaskInternalException("Tasks could not be upserted! Upserting will be retried.", e);
      }

      publishCountMetric(DD_ASPECT_UPSERT_TASKS_BY_KAFKA_EVENTS + inputTopic + ".TaskCountSuccess", numberOfTasksToUpsert);
   }

   private void publishCountMetric(String s, long numberOfTasksToUpsert) {
      
   }

   /**
    * Do tasks upsertion
    * This is the place where you may catch and treat exceptions for which tasks
    * upsertion shouldn't be retried (e.g in case of invalid tasks). Otherwise throw exception
    *
    * @param upsertTasksEvent
    */
   private void upsertTasks(UpsertTasksEvent upsertTasksEvent, UpsertTaskInputTopic inputTopic) {
      LocalDateTime startTime = LocalDateTime.now();

      try {
         upsertTasksInternal(upsertTasksEvent, inputTopic);
      } catch ( ResourceNotFoundException e) {
         LOGGER.error("Upserting tasks for lane {} failed with exception. " +
                      "Won't be retried as failure is unrecoverable for event: {}", inputTopic, upsertTasksEvent, e);
         publishIncrementMetric(DD_ASPECT_UPSERT_TASKS_BY_KAFKA_EVENTS + inputTopic + ".EventCountUnrecoverable");
      }

      publishHistogramMetric(DD_ASPECT_UPSERT_TASKS_BY_KAFKA_EVENTS + inputTopic + ".EventUpsertDuration",
          Duration.between(startTime, LocalDateTime.now()).toMillis());
   }

   private void publishHistogramMetric(String s, long toMillis) {
   }

   private void publishIncrementMetric(String s) {
      // Kafka?
   }

   private void upsertTasksInternal(UpsertTasksEvent upsertTasksEvent, UpsertTaskInputTopic inputTopic) {
      
   }
}
