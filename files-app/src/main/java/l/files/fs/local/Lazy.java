package l.files.fs.local;

public abstract class Lazy<T> {

    private boolean called;
    private T value;

    public T get() {
        if (!called) {
            called = true;
            value = doGet();
        }
        return value;
    }

    protected abstract T doGet();

}
