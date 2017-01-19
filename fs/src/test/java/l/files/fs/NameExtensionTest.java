package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import l.files.fs.Name;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class NameExtensionTest {

    private final Name name;
    private final String expectedExtension;

    public NameExtensionTest(String name, String expectedExtension) {
        this.name = Name.create(name);
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
        assertEquals(expectedExtension, name.extension());
    }
}
