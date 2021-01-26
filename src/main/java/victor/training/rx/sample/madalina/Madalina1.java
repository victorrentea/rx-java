package victor.training.rx.sample.madalina;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class Madalina1 {
   private static final Logger LOGGER = LoggerFactory.getLogger(Madalina1.class);
   private GetDocumentChunk getDocumentChunk;

   public InputStream writeDocumentContentToInputStream(DocumentMetadata documentMetaData) {
      LOGGER.info("READ document filename = {} / documentId = {} / size = {} / chunks = {}",
          documentMetaData.getFileName(), documentMetaData.getDocumentId(), documentMetaData.getDocumentTotalSize(),
          documentMetaData.getNoOfChunks());

      String fileName = documentMetaData.getFileName();
      UUID version = documentMetaData.getUpdatedOn();
      final IntStream range = IntStream.range(1, documentMetaData.getNoOfChunks() + 1);

      String escapedFileName = fileName != null ? fileName.replace("%", "%%") : null;
      ExecutorService executor = Executors.newFixedThreadPool(
          4,
          new ThreadFactoryBuilder().setDaemon(true)
              .setNameFormat("ChunkReader-" + escapedFileName + "-%d").build());

      ChunkProcessor chunkProcessor = new ChunkProcessor(documentMetaData.getNoOfChunks());

      final AtomicBoolean failure = new AtomicBoolean(false);

      Observable.from(range::iterator)
          .flatMap(integerObservable -> Observable.just(integerObservable)
              .map(integer -> getDocumentChunk.execute(fileName, version, integer)
                  .orElseThrow(ResourceNotFoundException::new))
              .subscribeOn(Schedulers.from(executor)))// TODO victor changes thread for the execute() above.
          .toBlocking() // TODO victor discuss
          .subscribe(chunkProcessor::processInputStream,
              throwable -> {
                 failure.set(true);
                 LOGGER.error("Error while processing the chunks {}", throwable.getMessage());
              },
              () ->
                  LOGGER.debug("Sent to inputStream chunks for file {} / version {} / chunks {}",
                      fileName, version, documentMetaData.getNoOfChunks())
          );

      executor.shutdown();

      if (failure.get())
         throw new DocumentDataNotFoundException("Document data not found for document " + documentMetaData.getFileName());

      return new SequenceInputStream(Collections.enumeration(chunkProcessor.getListOfInputStreams()));
   }



//
}
