package l.files.fs;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public abstract class FilePathParameterizedTest {

    final String path;

    FilePathParameterizedTest(String path) {
        this.path = path;
    }

    @Parameters(name = "\"{0}\"")
    public static Collection<Object[]> paths() {
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
                {"/a/\n✌️"},

                {"\\"},
        });
    }

}
