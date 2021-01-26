package victor.training.rx.sample.madalina;

import java.util.List;

public interface BatchAcknowledgingMessageListener<T, T1> {
   void listen(List<ConsumerRecord<String, UpsertTasksEvent>> consumerRecords, Ack ack);
}
