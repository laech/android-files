package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class NameDotExtensionTest {

    private final String name;

    public NameDotExtensionTest(String name) {
        this.name = name;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {"."},
                {".."},
                {"....."},
                {"pic"},
                {" pic "},
                {"pic.png"},
                {"pic.png"},
                {"pic. png"},
                {" pic. png"},
                {" pic . png"},
                {".pic.png"},
                {". pic.png"},
                {"pic.abc.png"},
                {".pic.abc.png"},
        });
    }

    @Test
    public void dot_extension_is_expected() throws Exception {
        Name nameObject = Name.fromString(name);
        assertEquals(nameObject.base() + nameObject.dotExtension(), name);
    }
}
