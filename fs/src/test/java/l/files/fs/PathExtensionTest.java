package l.files.fs;

import l.files.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class PathExtensionTest {

    private final Path name;
    private final String expectedExtension;

    public PathExtensionTest(String name, String expectedExtension) {
        this.name = Path.of(name);
        this.expectedExtension = expectedExtension;
    }

    @Parameters(name = "\"{0}\".extension() == \"{1}\"")
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {"pic.", ""},
                {"pic.png.", ""},
                {"pic.png..", ""},
                {"pic.png...", ""},
                {" ...", ""},
                {".", ""},
                {"..", ""},
                {"...", ""},
                {".a", ""},
                {".abc", ""},
                {". ", ""},
                {". hello world", ""},
                {"pic.png", "png"},
                {"pic.abc.png", "png"},
                {"a.b.c.d.e.f", "f"},
                {" .png", "png"},
                {"hello world.pdf", "pdf"},
        });
    }

    @Test
    public void extension_is_expected() throws Exception {
        assertEquals(Optional.of(expectedExtension), name.getExtension());
    }
}
