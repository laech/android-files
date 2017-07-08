package l.files.base;

import javax.annotation.Nullable;

import static l.files.base.Objects.requireNonNull;

public abstract class Optional<A> implements Foldable<A> {

    private Optional() {
    }

    public abstract <B> Optional<B> map(Function<? super A, ? extends B> f);

    public abstract A or(A other);

    public abstract Object orObject(Object other);

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> empty() {
        return (Optional<T>) NOTHING;
    }

    public static <T> Optional<T> of(T value) {
        return new Just<>(value);
    }

    public static <T> Optional<T> ofNullable(@Nullable T value) {
        return value == null ? Optional.<T>empty() : of(value);
    }

    private static final class Just<T> extends Optional<T> {

        private final T value;

        private Just(T value) {
            this.value = requireNonNull(value);
        }

        @Override
        public <B> Optional<B> map(Function<? super T, ? extends B> f) {
            return ofNullable(f.apply(value));
        }

        @Override
        public T or(T other) {
            return value;
        }

        @Override
        public Object orObject(Object other) {
            return value;
        }

        @Override
        public <B> B fold(B init, BiFunction<B, T, B> f) {
            return f.apply(init, value);
        }
    }

    private static Optional<?> NOTHING = new Optional<Object>() {

        @Override
        public <B> Optional<B> map(Function<? super Object, ? extends B> f) {
            return empty();
        }

        @Override
        public Object or(Object other) {
            return other;
        }

        @Override
        public Object orObject(Object other) {
            return other;
        }

        @Override
        public <B> B fold(B init, BiFunction<B, Object, B> f) {
            return init;
        }
    };
}
