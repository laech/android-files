package l.files.base;

public final class Longs {

    private Longs() {
    }

    public static int compare(long a, long b) {
        return a < b ? -1 : (a == b ? 0 : 1);
    }

}
