package l.files.base.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static l.files.base.io.Readers.readString;
import static org.junit.Assert.assertEquals;

public final class ReadersTest {

    private final Charset utf8 = Charset.forName("UTF-8");
    private final Charset ascii = Charset.forName("US-ASCII");

    @Test
    public void readString_single_charset_read_full_string() throws Exception {
        testReadString("hello", ascii, Integer.MAX_VALUE, "hello", utf8);
    }

    @Test
    public void readString_single_charset_read_partial_string() throws Exception {
        testReadString("hello", ascii, 1, "h", utf8);
    }

    @Test
    public void readString_single_charset_read_empty_string() throws Exception {
        testReadString("hello", ascii, 0, "", utf8);
    }

    @Test
    public void readString_multi_charset_read_full_string() throws Exception {
        String src = repeat("a", 8192) + repeat("你", 8192);
        testReadString(src, utf8, Integer.MAX_VALUE, src, ascii, utf8);
    }

    @Test
    public void readString_multi_charset_read_partial_string() throws Exception {
        String src = repeat("a", 8192) + repeat("你", 8192);
        String expected = repeat("a", 8192) + "你";
        testReadString(src, utf8, 8193, expected, ascii, utf8);
    }

    @Test
    public void readString_multi_charset_read_empty_string() throws Exception {
        String src = repeat("a", 8192) + repeat("你", 8192);
        testReadString(src, utf8, 0, "", ascii, utf8);
    }

    private void testReadString(
            String src,
            Charset srcCharset,
            int limit,
            String expected,
            Charset... charsets) throws IOException {

        InputStream in = new ByteArrayInputStream(src.getBytes(srcCharset)) {
            @Override
            public boolean markSupported() {
                return false;
            }
        };

        String actual = readString(in, limit, charsets);
        assertEquals(expected, actual);
    }

    private String repeat(String s, int n) {
        StringBuilder builder = new StringBuilder(s.length() * n);
        for (int i = 0; i < n; i++) {
            builder.append(s);
        }
        return builder.toString();
    }

}
