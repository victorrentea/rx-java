package victor.training.rx.madalina;

import java.util.List;
import java.util.function.BiConsumer;

public interface BatchAcknowledgingMessageListener<T, T1> {
   void listen(List<ConsumerRecord<String, UpsertTasksEvent>> consumerRecords, Ack ack);
}
