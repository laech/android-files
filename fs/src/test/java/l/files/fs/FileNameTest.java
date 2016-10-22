package l.files.fs;

import org.junit.Test;

import static l.files.fs.Files.UTF_8;
import static org.junit.Assert.assertEquals;

public final class FileNameTest {

    private void testBaseDotExtension(String value) {
        FileName name = FileName.fromBytes(value.getBytes(UTF_8));
        assertEquals(value, name.base() + name.dotExtension());
    }

    private void testNameHasBase(String name, String base) {
        assertEquals(base, FileName.fromBytes(name.getBytes(UTF_8)).base());
    }

    private void testNameHasExtension(String name, String ext) {
        assertEquals(ext, FileName.fromBytes(name.getBytes(UTF_8)).extension());
    }

    @Test
    public void if_name_ends_with_a_dot_then_base_is_whole_name() {
        testNameHasBase("pic.", "pic.");
        testNameHasBase("pic.png.", "pic.png.");
        testNameHasBase("pic.png..", "pic.png..");
        testNameHasBase("pic.png...", "pic.png...");
        testNameHasBase(" ...", " ...");
    }

    @Test
    public void if_name_ends_with_a_dot_then_extension_is_empty() {
        testNameHasExtension("pic.", "");
        testNameHasExtension("pic.png.", "");
        testNameHasExtension("pic.png..", "");
        testNameHasExtension("pic.png...", "");
        testNameHasExtension(" ...", "");
    }

    @Test
    public void if_name_contains_only_dots_then_base_is_whole_name() {
        testNameHasBase(".", ".");
        testNameHasBase("..", "..");
        testNameHasBase("...", "...");
    }

    @Test
    public void if_name_contains_only_dots_then_extension_is_empty() {
        testNameHasExtension(".", "");
        testNameHasExtension("..", "");
        testNameHasExtension("...", "");
    }

    @Test
    public void if_the_only_dot_is_at_start_then_base_is_whole_name() {
        testNameHasBase(".a", ".a");
        testNameHasBase(".abc", ".abc");
        testNameHasBase(". ", ". ");
        testNameHasBase(". hello world", ". hello world");
    }

    @Test
    public void if_the_only_dot_is_at_start_then_extension_is_empty() {
        testNameHasExtension(".a", "");
        testNameHasExtension(".abc", "");
        testNameHasExtension(". ", "");
        testNameHasExtension(". hello world", "");
    }

    @Test
    public void if_last_dot_is_not_at_start_or_end_then_base_is_substring_before_it() {
        testNameHasBase("pic.png", "pic");
        testNameHasBase("pic.abc.png", "pic.abc");
        testNameHasBase("a.b.c.d.e.f", "a.b.c.d.e");
        testNameHasBase(" .png", " ");
        testNameHasBase("hello world.pdf", "hello world");
    }

    @Test
    public void if_last_dot_is_not_at_start_or_end_then_extension_is_substring_after_it() {
        testNameHasExtension("pic.png", "png");
        testNameHasExtension("pic.abc.png", "png");
        testNameHasExtension("a.b.c.d.e.f", "f");
        testNameHasExtension(" .png", "png");
        testNameHasExtension("hello world.pdf", "pdf");
    }

    @Test
    public void if_name_is_empty_then_base_is_empty() throws Exception {
        testNameHasBase("", "");
    }

    @Test
    public void if_name_is_empty_extension_is_empty() throws Exception {
        testNameHasExtension("", "");
    }

    @Test
    public void name_is_made_up_of_base_dot_extension() throws Exception {
        testBaseDotExtension("");
        testBaseDotExtension(".");
        testBaseDotExtension("..");
        testBaseDotExtension(".....");
        testBaseDotExtension("pic");
        testBaseDotExtension(" pic ");
        testBaseDotExtension("pic.png");
        testBaseDotExtension("pic.png");
        testBaseDotExtension("pic. png");
        testBaseDotExtension(" pic. png");
        testBaseDotExtension(" pic . png");
        testBaseDotExtension(".pic.png");
        testBaseDotExtension(". pic.png");
        testBaseDotExtension("pic.abc.png");
        testBaseDotExtension(".pic.abc.png");
    }

}
