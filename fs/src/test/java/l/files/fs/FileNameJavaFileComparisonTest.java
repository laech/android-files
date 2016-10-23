package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class FileNameJavaFileComparisonTest {

    private final String path;

    public FileNameJavaFileComparisonTest(String path) {
        this.path = path;
    }

    @Parameters(name = "\"{0}\"")
    public static Collection<Object[]> data() {
        return asList(new Object[][]{

                {""},
                {" "},
                {"\t"},
                {"\n"},

                {"/"},
                {"//"},

                {"."},
                {"./."},
                {".."},
                {"../."},
                {"../.."},

                {"a"},
                {"a/b"},
                {"a//b"},
                {"a//b/"},
                {"a//b//"},
                {"a/b/"},

                {"//a"},
                {"/a"},
                {"/a/hello world"},
                {"/a/你好"},
                {"/a/✌️"},

                {"\\"},
        });
    }

    @Test
    public void equals_java_io_file_name() throws Exception {
        String expected = new File(path).getName();
        String actual = Path.fromString(path).name().toString();
        assertEquals(expected, actual);
    }
}
