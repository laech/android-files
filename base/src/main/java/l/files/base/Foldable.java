package l.files.base;

public interface Foldable<A> {

    <B> B fold(B init, BiFunction<B, A, B> f);

}
