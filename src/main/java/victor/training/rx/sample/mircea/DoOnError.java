package victor.training.rx.sample.mircea;

import javafx.collections.ObservableList;
import rx.Completable;
import rx.Observable;
import victor.training.rx.sample.opp2.BadRequestException;

public class DoOnError {
   public static void main(String[] args) {

      Observable.error(new RuntimeException())
          .doOnError(e -> System.out.println("1:"+e))
          .doOnError(e -> System.out.println("2:"+e))
      .subscribe();

   }
}
