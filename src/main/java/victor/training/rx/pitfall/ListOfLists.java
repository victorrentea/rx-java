package victor.training.rx.pitfall;

import rx.Observable;

import static java.util.Arrays.asList;

public class ListOfLists {
    public static void main(String[] args) {
        // TODO preserve the structure: I want  [[2,4],[6,8]] at the end
        Observable.just(asList(1, 2), asList(3, 4))
                .flatMap(list -> Observable.from(list))
                .map(n -> n * 2)
                .toList()
                .subscribe(System.out::println);
    }
}
