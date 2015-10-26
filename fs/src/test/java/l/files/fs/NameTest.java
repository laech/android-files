package l.files.fs;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.util.Collections.reverse;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class NameTest {

    private void testBaseDotExt(String value) {
        FileName name = FileName.of(value);
        assertEquals(value, name.base() + name.dotExt());
    }

    private void testNameHasBase(String name, String base) {
        assertEquals(base, FileName.of(name).base());
    }

    private void testNameHasExt(String name, String ext) {
        assertEquals(ext, FileName.of(name).ext());
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
    public void if_name_ends_with_a_dot_then_ext_is_empty() {
        testNameHasExt("pic.", "");
        testNameHasExt("pic.png.", "");
        testNameHasExt("pic.png..", "");
        testNameHasExt("pic.png...", "");
        testNameHasExt(" ...", "");
    }

    @Test
    public void if_name_contains_only_dots_then_base_is_whole_name() {
        testNameHasBase(".", ".");
        testNameHasBase("..", "..");
        testNameHasBase("...", "...");
    }

    @Test
    public void if_name_contains_only_dots_then_ext_is_empty() {
        testNameHasExt(".", "");
        testNameHasExt("..", "");
        testNameHasExt("...", "");
    }

    @Test
    public void if_the_only_dot_is_at_start_then_base_is_whole_name() {
        testNameHasBase(".a", ".a");
        testNameHasBase(".abc", ".abc");
        testNameHasBase(". ", ". ");
        testNameHasBase(". hello world", ". hello world");
    }

    @Test
    public void if_the_only_dot_is_at_start_then_ext_is_empty() {
        testNameHasExt(".a", "");
        testNameHasExt(".abc", "");
        testNameHasExt(". ", "");
        testNameHasExt(". hello world", "");
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
    public void if_last_dot_is_not_at_start_or_end_then_ext_is_substring_after_it() {
        testNameHasExt("pic.png", "png");
        testNameHasExt("pic.abc.png", "png");
        testNameHasExt("a.b.c.d.e.f", "f");
        testNameHasExt(" .png", "png");
        testNameHasExt("hello world.pdf", "pdf");
    }

    @Test
    public void if_name_is_empty_then_base_is_empty() throws Exception {
        testNameHasBase("", "");
    }

    @Test
    public void if_name_is_empty_ext_is_empty() throws Exception {
        testNameHasExt("", "");
    }

    @Test
    public void name_is_made_up_of_base_dot_ext() throws Exception {
        testBaseDotExt("");
        testBaseDotExt(".");
        testBaseDotExt("..");
        testBaseDotExt(".....");
        testBaseDotExt("pic");
        testBaseDotExt(" pic ");
        testBaseDotExt("pic.png");
        testBaseDotExt("pic.png");
        testBaseDotExt("pic. png");
        testBaseDotExt(" pic. png");
        testBaseDotExt(" pic . png");
        testBaseDotExt(".pic.png");
        testBaseDotExt(". pic.png");
        testBaseDotExt("pic.abc.png");
        testBaseDotExt(".pic.abc.png");
    }

    @Test
    public void comparator_is_locale_sensitive() throws Exception {
        testSort(ENGLISH, "a", "A", "b");
        testSort(SIMPLIFIED_CHINESE, "爱", "你好", "知道");
    }

    private void testSort(Locale locale, String... names) {
        List<FileName> expected = names(names);
        List<FileName> actual = new ArrayList<>(expected);

        reverse(actual);
        if (Objects.equals(expected, actual)) {
            fail();
        }

        sort(actual, FileName.comparator(locale));
        assertEquals(expected, actual);
    }

    private List<FileName> names(String... names) {
        List<FileName> result = new ArrayList<>(names.length);
        for (String name : names) {
            result.add(FileName.of(name));
        }
        return unmodifiableList(result);
    }

}
