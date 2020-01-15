package victor.training.rx;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GroupingItems {
    public static void main(String[] args) {
        sendWonPoint(Arrays.asList("A", "B", "B", "A", "B", "B", "A", "A", "A"));
    }

    private static void sendWonPoint(List<String> wonSequence) {
        int[] points = new int[2];
        Observable.from(wonSequence)
                .groupBy(p -> "A".equals(p) ? 1 : 0)
                .subscribe(group -> {
                    log.debug("Emit Group " + group);
                    Integer playerIndex = group.getKey();
                    group.subscribe(p -> points[playerIndex]++);
                });
        System.out.println(Arrays.toString(points));
    }
}
