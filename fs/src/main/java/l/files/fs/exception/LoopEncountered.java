package l.files.fs.exception;

import java.io.IOException;

public final class LoopEncountered extends IOException {

    public LoopEncountered(String message, Throwable cause) {
        super(message, cause);
    }

}
