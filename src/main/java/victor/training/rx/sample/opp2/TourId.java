package victor.training.rx.sample.opp2;

public class TourId {
   private final String value;

   public TourId(String value) {
      this.value = value;
   }

   public static TourId fromString(String tourId) {
      return new TourId(tourId);
   }

   public String asString() {
      return value;
   }
}
