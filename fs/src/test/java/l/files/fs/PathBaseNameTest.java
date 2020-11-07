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
public final class PathBaseNameTest {

    private final Path name;
    private final String expectedBaseName;

    public PathBaseNameTest(String name, String expectedBaseName) {
        this.name = Path.of(name);
        this.expectedBaseName = expectedBaseName;
    }

    @Parameters(name = "\"{0}\".base() == \"{1}\"")
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {"pic.", "pic."},
                {"pic.png.", "pic.png."},
                {"pic.png..", "pic.png.."},
                {"pic.png...", "pic.png..."},
                {" .", " ."},
                {" ..", " .."},
                {" ...", " ..."},
                {".a", ".a"},
                {".abc", ".abc"},
                {". ", ". "},
                {". hello world", ". hello world"},
                {"pic.png", "pic"},
                {"pic.abc.png", "pic.abc"},
                {"a.b.c.d.e.f", "a.b.c.d.e"},
                {" .png", " "},
                {"hello world.pdf", "hello world"},
        });
    }

    @Test
    public void base_name_is_expected() throws Exception {
        assertEquals(Optional.of(expectedBaseName), name.getBaseName());
    }
}
