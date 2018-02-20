package l.files.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public final class Lists {
    private Lists() {
    }

    public static <A, B> List<B> map(Collection<A> from, Function<A, B> f) {
        List<B> to = new ArrayList<>(from.size());
        for (A input : from) {
            to.add(f.apply(input));
        }
        return unmodifiableList(to);
    }
}
