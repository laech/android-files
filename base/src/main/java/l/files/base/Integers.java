package l.files.base;

public final class Integers {

    private Integers() {
    }

    public static int compare(int a, int b) {
        return a < b ? -1 : (a == b ? 0 : 1);
    }

}
