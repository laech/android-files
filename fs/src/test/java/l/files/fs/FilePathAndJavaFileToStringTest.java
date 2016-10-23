package l.files.fs;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public final class FilePathAndJavaFileToStringTest
        extends FilePathParameterizedTest {

    public FilePathAndJavaFileToStringTest(String path) {
        super(path);
    }

    @Test
    public void equals_java_io_file_string() throws Exception {
        String expected = new File(path).toString();
        String actual = Path.fromString(path).toString();
        assertEquals(expected, actual);
    }
}
