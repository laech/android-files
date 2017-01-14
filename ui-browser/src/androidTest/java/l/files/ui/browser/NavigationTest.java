package l.files.ui.browser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

import l.files.base.Consumer;
import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.Stat;

import static android.os.Build.VERSION.SDK_INT;
import static android.test.MoreAsserts.assertNotEqual;
import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_NO_YEAR;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.formatDateTime;
import static android.text.format.Formatter.formatShortFileSize;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.testing.fs.ExtendedFileSystem.UTF_8;
import static l.files.ui.browser.FileSort.NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public final class NavigationTest extends BaseFilesActivityTest {

    @Test
    public void can_see_file_renamed_to_different_casing() throws Exception {

        Path dir;
        try {
            dir = createCaseInsensitiveFileSystemDir("can_see_file_renamed_to_different_casing");
        } catch (CannotRenameFileToDifferentCasing e) {
            Log.d("NavigationTest", "skipping test_can_see_file_renamed_to_different_casing()", e);
            return;
        }

        try {

            Path src = fs.createDir(dir.concat("a"));
            Path dst = dir.concat("A");

            screen().assertAllItemsDisplayedInOrder(src);

            fs.move(src, dst);

            screen()
                    .sort()
                    .by(NAME)
                    .assertAllItemsDisplayedInOrder(dst);

        } finally {
            fs.deleteRecursiveIfExists(dir);
        }
    }

    @Test
    public void can_start_from_data_uri() throws Exception {
        Path dir = fs.createDirs(dir().concat("dir"));
        Path file = fs.createFile(dir.concat("file"));
        setActivityIntent(new Intent().setData(Uri.fromFile(dir.toFile())));
        screen()
                .assertCurrentDirectory(dir)
                .assertListViewContains(file, true);
    }

    @Test
    public void can_preview() throws Exception {
        Path dir = fs.createDir(dir().concat("test_can_preview"));
        Path empty = fs.createFile(dir.concat("empty"));
        Path file = dir.concat("file");
        Path link = fs.createSymbolicLink(dir.concat("link"), file);
        fs.writeUtf8(file, "hello");
        screen()
                .clickInto(dir)
                .assertThumbnailShown(file, true)
                .assertThumbnailShown(link, true)
                .assertThumbnailShown(empty, false);
    }

    @Test
    public void can_navigate_into_non_utf8_named_dir() throws Exception {

        byte[] nonUtf8Name = {-19, -96, -67, -19, -80, -117};
        assertNotEqual(
                nonUtf8Name.clone(),
                new String(nonUtf8Name.clone(), UTF_8).getBytes(UTF_8)
        );

        Path nonUtf8NamedDir = fs.createDir(dir().concat(nonUtf8Name));
        Path child = fs.createFile(nonUtf8NamedDir.concat("a"));

        screen()
                .clickInto(nonUtf8NamedDir)
                .assertListViewContains(child, true);
    }

    @Test
    public void can_shows_dirs_with_same_name_but_different_name_bytes() throws Exception {

        byte[] notUtf8 = {-19, -96, -67, -19, -80, -117};
        byte[] utf8 = new String(notUtf8, UTF_8).getBytes(UTF_8);

        assertFalse(Arrays.equals(notUtf8, utf8));
        assertEquals(new String(notUtf8, UTF_8), new String(utf8, UTF_8));

        Path notUtf8Dir = fs.createDir(dir().concat(notUtf8));
        Path notUtf8Child = fs.createFile(notUtf8Dir.concat("notUtf8"));

        Path utf8Dir = fs.createDir(dir().concat(utf8));
        Path utf8Child = fs.createFile(utf8Dir.concat("utf8"));

        screen()
                .assertListViewContains(notUtf8Dir, true)
                .assertListViewContains(utf8Dir, true)

                .clickInto(notUtf8Dir)
                .assertListViewContains(notUtf8Child, true)
                .assertListViewContains(utf8Child, false)

                .pressBack()
                .clickInto(utf8Dir)
                .assertListViewContains(utf8Child, true)
                .assertListViewContains(notUtf8Child, false);
    }

    @Test
    public void can_navigate_into_etc_proc_self_fdinfo_without_crashing()
            throws Exception {

        assumeTrue("Skipping test, no permission to read /proc on Android N",
                SDK_INT != 24);
//                SDK_INT != N); // TODO Change to 'N' after upgrade to API 24

        screen().selectFromNavigationMode(Path.fromString("/"));
        screen().clickInto(Path.fromString("/proc"));
        screen().clickInto(Path.fromString("/proc/self"));
        screen().clickInto(Path.fromString("/proc/self/fdinfo"));
    }

    @Test
    public void can_navigate_through_title_list_drop_down() throws Exception {
        Path parent = dir().parent();
        assert parent != null;
        screen()
                .selectFromNavigationMode(parent)
                .assertNavigationModeHierarchy(parent);
    }

    @Test
    public void updates_navigation_list_when_going_into_a_new_dir() throws Exception {
        screen().assertNavigationModeHierarchy(dir());
        Path dir = fs.createDir(dir().concat("dir"));
        screen().clickInto(dir).assertNavigationModeHierarchy(dir);
    }

    @Test
    public void shows_size_only_if_unable_to_determine_modified_date() throws Exception {
        Path file = fs.createFile(dir().concat("file"));
        fs.setLastModifiedTime(file, NOFOLLOW, EPOCH);

        long size = fs.stat(file, NOFOLLOW).size();
        Context c = getActivity();
        final String expected = formatShortFileSize(c, size);

        screen().assertSummary(file, new Consumer<String>() {
            @Override
            public void accept(String summary) {
                assertTrue(summary.contains(expected));
            }
        });
    }

    @Test
    public void shows_time_and_size_for_file() throws Exception {
        Path file = fs.createFile(dir().concat("file"));
        fs.appendUtf8(file, file.toString());

        Context c = getActivity();
        Stat stat = fs.stat(file, NOFOLLOW);
        long lastModifiedMillis = stat.lastModifiedTime().to(MILLISECONDS);
        String date = getTimeFormat(c).format(new Date(lastModifiedMillis));
        String size = formatShortFileSize(c, stat.size());
        String expected = c.getString(R.string.x_dot_y, date, size);
        screen().assertSummary(file, expected);
    }

    @Test
    public void shows_time_only_for_today() throws Exception {
        long time = currentTimeMillis();
        DateFormat format = getTimeFormat(getActivity());
        String expected = format.format(new Date(time));
        testDirectorySummary(expected, time);
    }

    @Test
    public void shows_time_as_month_day_for_date_of_current_year() throws Exception {
        long time = currentTimeMillis() - DAYS.toMillis(2);
        int flags
                = FORMAT_SHOW_DATE
                | FORMAT_ABBREV_MONTH
                | FORMAT_NO_YEAR;
        String expected = formatDateTime(getActivity(), time, flags);
        testDirectorySummary(expected, time);
    }

    @Test
    public void shows_time_as_year_month_day_for_date_outside_of_current_year() throws Exception {
        long time = currentTimeMillis() - DAYS.toMillis(400);
        DateFormat format = getDateFormat(getActivity());
        String expected = format.format(new Date(time));
        testDirectorySummary(expected, time);
    }

    private void testDirectorySummary(
            String expected, long modifiedAt) throws Exception {
        Path d = fs.createDir(dir().concat("dir"));
        fs.setLastModifiedTime(d, NOFOLLOW, Instant.of(modifiedAt / 1000, 0));
        screen().assertSummary(d, expected);
    }

    @Test
    public void directory_view_is_disabled_if_no_read_permission() throws Exception {
        Path dir = fs.createDir(dir().concat("dir"));
        fs.removePermissions(dir, Permission.read());
        screen().assertDisabled(dir);
    }

    @Test
    public void link_displayed() throws Exception {
        Path dir = fs.createDir(dir().concat("dir"));
        Path link = fs.createSymbolicLink(dir().concat("link"), dir);

        screen()
                .assertLinkPathDisplayed(dir, null)
                .assertLinkPathDisplayed(link, dir);
    }

    @Test
    public void can_see_changes_in_parent_directory() throws Exception {

        Path level1Dir = dir();
        Path level2Dir = fs.createDir(level1Dir.concat("level2Dir"));
        Path level3Dir = fs.createDir(level2Dir.concat("level3Dir"));
        screen()
                .sort()
                .by(NAME)
                .clickInto(level2Dir)
                .clickInto(level3Dir)
                .assertCurrentDirectory(level3Dir);

        Path level3File = fs.createFile(level2Dir.concat("level3File"));
        Path level2File = fs.createFile(level1Dir.concat("level2File"));
        screen()
                .pressBack()
                .assertAllItemsDisplayedInOrder(level3Dir, level3File)
                .pressBack()
                .assertAllItemsDisplayedInOrder(level2Dir, level2File);
    }

    @Test
    public void can_see_changes_in_linked_directory() throws Exception {
        Path dir = fs.createDir(dir().concat("dir"));
        Path link = fs.createSymbolicLink(dir().concat("link"), dir);
        screen()
                .clickInto(link)
                .assertCurrentDirectory(link);

        Path child = fs.createDir(link.concat("child"));
        screen()
                .clickInto(child)
                .assertCurrentDirectory(child);
    }

    @Test
    public void press_action_bar_up_indicator_will_go_back() throws Exception {
        Path dir = fs.createDir(dir().concat("dir"));
        Path parent = dir.parent();
        assert parent != null;
        screen()
                .clickInto(dir)
                .assertCurrentDirectory(dir)
                .pressActionBarUpIndicator()
                .assertCurrentDirectory(parent);
    }

    @Test
    public void action_bar_title_shows_name_of_directory() throws Exception {
        screen()
                .clickInto(fs.createDir(dir().concat("a")))
                .assertActionBarTitle("a");
    }

    @Test
    public void action_bar_hides_up_indicator_when_there_is_no_back_stack_initially() {
        screen().assertActionBarUpIndicatorIsVisible(false);
    }

    @Test
    public void action_bar_shows_up_indicator_when_there_is_back_stack() throws Exception {
        screen()
                .clickInto(fs.createDir(dir().concat("dir")))
                .assertActionBarUpIndicatorIsVisible(true);
    }

    @Test
    public void action_bar_hides_up_indicator_when_there_is_no_back_stack_to_go_back_to() throws Exception {
        screen()
                .clickInto(fs.createDir(dir().concat("dir")))
                .pressBack()
                .assertActionBarUpIndicatorIsVisible(false);
    }

    @Test
    public void long_press_back_will_clear_back_stack() throws Exception {
        screen()
                .clickInto(fs.createDir(dir().concat("a")))
                .clickInto(fs.createDir(dir().concat("a/b")))
                .clickInto(fs.createDir(dir().concat("a/b/c")))
                .longPressBack()
                .assertCurrentDirectory(dir());
    }

    @Test
    public void observes_on_current_directory_and_shows_added_deleted_files() throws Exception {
        Path a = fs.createDir(dir().concat("a"));
        screen().assertListViewContains(a, true);

        Path b = fs.createFile(dir().concat("b"));
        screen()
                .assertListViewContains(a, true)
                .assertListViewContains(b, true);

        fs.delete(b);
        screen()
                .assertListViewContains(a, true)
                .assertListViewContains(b, false);
    }

    @Test
    public void updates_view_on_child_directory_modified() throws Exception {
        Path dir = fs.createDir(dir().concat("a"));
        testUpdatesDateViewOnChildModified(dir);
    }

    @Test
    public void updates_view_on_child_file_modified() throws Exception {
        Path file = fs.createFile(dir().concat("a"));
        testUpdatesDateViewOnChildModified(file);
        testUpdatesSizeViewOnChildModified(file);
    }

    private void testUpdatesSizeViewOnChildModified(Path file)
            throws IOException {
        fs.setLastModifiedTime(file, NOFOLLOW, EPOCH);

        final CharSequence[] chars = {null};
        screen().assertSummary(file, new Consumer<String>() {
            @Override
            public void accept(String summary) {
                chars[0] = summary;
            }
        });

        modify(file);

        screen().assertSummary(file, new Consumer<String>() {
            @Override
            public void accept(String summary) {
                assertNotEqual(chars[0], summary);
            }
        });
    }

    private void testUpdatesDateViewOnChildModified(Path file)
            throws IOException {

        fs.setLastModifiedTime(file, NOFOLLOW, Instant.of(100000, 1));

        final CharSequence[] date = {null};
        screen().assertSummary(file, new Consumer<String>() {
            @Override
            public void accept(String summary) {
                date[0] = summary;
            }
        });

        modify(file);

        screen().assertSummary(file, new Consumer<String>() {
            @Override
            public void accept(String summary) {
                assertNotEqual(date[0], summary);
            }
        });
    }

    private Path modify(Path file) throws IOException {
        Stat stat = fs.stat(file, NOFOLLOW);
        Instant lastModifiedBefore = stat.lastModifiedTime();
        if (stat.isDirectory()) {
            fs.createDir(file.concat(String.valueOf(nanoTime())));
        } else {
            fs.appendUtf8(file, "test");
        }
        Instant lastModifiedAfter = fs.stat(file, NOFOLLOW).lastModifiedTime();
        assertNotEqual(lastModifiedBefore, lastModifiedAfter);
        return file;
    }

}
