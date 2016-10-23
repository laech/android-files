package l.files.fs;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public final class FileNameJavaFileComparisonTest
        extends FilePathParameterizedTest {

    public FileNameJavaFileComparisonTest(String path) {
        super(path);
    }

    @Test
    public void equals_java_io_file_name() throws Exception {
        String expected = new File(path).getName();
        String actual = Path.fromString(path).name().toString();
        assertEquals(expected, actual);
    }
}
