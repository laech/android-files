package l.files.fs;

public final class Strings {

    private Strings() {
    }


    public static String repeat(String str, int times) {
        StringBuilder builder = new StringBuilder(str.length() * times);
        for (int i = 0; i < times; i++) {
            builder.append(str);
        }
        return builder.toString();
    }
}
