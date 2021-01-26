package victor.training.rx.sample.madalina;

import java.io.InputStream;
import java.util.List;

public class ChunkProcessor {
   private int currentChunkId;
   private List<InputStream> listOfInputStreams;
   private DocumentChunk[] unprocessedChunks;

   public ChunkProcessor(int noOfChunks) {

   }

   public void processInputStream(DocumentChunk chunk) {

      try {
         final int chunkID = chunk.getChunkPosition() - 1;
         if (currentChunkId == chunkID) {
            InputStream inputStream = writeToInputStream(chunk);
            listOfInputStreams.add(inputStream);

            currentChunkId++;
            if (currentChunkId < unprocessedChunks.length)
               lookAheadInputStream();

         } else {
            if (chunkID > currentChunkId) {
               unprocessedChunks[chunkID] = chunk;
            }
         }
      } catch (Exception ex) {
//         logger.error(errorMessage, ex);
      }
   }

   private void lookAheadInputStream() {

   }

   private InputStream writeToInputStream(DocumentChunk chunk) {
      return null;
   }

   public List<InputStream> getListOfInputStreams() {
      return null;
   }
}
