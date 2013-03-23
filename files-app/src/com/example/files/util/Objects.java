package com.example.files.util;

public final class Objects {

    public static <T> T requires(T arg, String msg) {
        if (arg == null) throw new NullPointerException(msg);
        return arg;
    }

    private Objects() {
    }
}
