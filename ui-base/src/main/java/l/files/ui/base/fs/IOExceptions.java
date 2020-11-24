package l.files.ui.base.fs;

import java.io.IOException;


public final class IOExceptions {

    private IOExceptions() {
    }

    public static String message(IOException exception) {
        return exception.getMessage();
    }

}
