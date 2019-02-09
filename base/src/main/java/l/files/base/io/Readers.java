package l.files.base.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.MalformedInputException;

import androidx.annotation.Nullable;

import static java.lang.Math.min;
import static java.nio.charset.CodingErrorAction.REPORT;

public final class Readers {

    private Readers() {
    }

    /**
     * Tries each charset in sequence for decoding the
     * input stream, until one is found that can read at most
     * {@code limit} number of characters successfully, and
     * the resulting string of the read is returned. Returns
     * null if none is successful.
     */
    @Nullable
    public static String readString(
            InputStream in, int limit, Charset... charsets) throws IOException {

        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        in.mark(Integer.MAX_VALUE);

        for (Charset charset : charsets) {
            try {
                return readStringWithCharset(in, limit, charset);
            } catch (MalformedInputException ignored) {
                in.reset();
            }
        }

        return null;
    }

    private static String readStringWithCharset(
            InputStream in, int limit, Charset charset) throws IOException {

        StringBuilder builder = new StringBuilder();
        Reader reader = new InputStreamReader(in, newThrowingDecoder(charset));
        char[] buffer = new char[1024];
        while (true) {
            int len = min(buffer.length, limit - builder.length());
            int count = reader.read(buffer, 0, len);
            if (count == -1) {
                return builder.toString();
            }
            builder.append(buffer, 0, count);
            if (builder.length() >= limit) {
                return builder.toString();
            }
        }
    }

    private static CharsetDecoder newThrowingDecoder(Charset charset) {
        return charset.newDecoder()
                .onUnmappableCharacter(REPORT)
                .onMalformedInput(REPORT);
    }

}
