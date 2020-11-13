package l.files.operations;

import l.files.testing.fs.PathBaseTest;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public final class SizeTest extends PathBaseTest {

    @Test
    public void size() throws Exception {
        Path a = createDirectory(dir1().toJavaPath().resolve("a"));
        Path b = createFile(dir1().toJavaPath().resolve("a/b"));
        Path c = createFile(dir1().toJavaPath().resolve("c"));
        Path d = createDirectory(dir1().toJavaPath().resolve("d"));

        Size size = new Size(asList(a, b, c, d));
        size.execute();

        long expected =
            Files.size(a) + Files.size(b) + Files.size(c) + Files.size(d);
        assertEquals(expected, size.getSize());
    }

}
