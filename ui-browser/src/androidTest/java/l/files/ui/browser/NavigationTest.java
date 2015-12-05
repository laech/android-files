package l.files.ui.browser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.junit.Test;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.Stat;
import l.files.fs.local.LocalPath;

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
import static l.files.fs.Files.UTF_8;
import static l.files.fs.Files.appendUtf8;
import static l.files.fs.Files.createDir;
import static l.files.fs.Files.createDirs;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.createLink;
import static l.files.fs.Files.delete;
import static l.files.fs.Files.deleteRecursiveIfExists;
import static l.files.fs.Files.move;
import static l.files.fs.Files.removePermissions;
import static l.files.fs.Files.setLastModifiedTime;
import static l.files.fs.Files.stat;
import static l.files.fs.Files.writeUtf8;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.FileSort.NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public final class NavigationTest extends BaseFilesActivityTest {

    @Test
    public void can_see_file_renamed_to_different_casing() throws Exception {

        Path dir = createCaseInsensitiveFileSystemDir();
        try {

            Path src = createDir(dir.resolve("a"));
            Path dst = dir.resolve("A");

            screen().assertAllItemsDisplayedInOrder(src);

            move(src, dst);

            screen()
                    .sort()
                    .by(NAME)
                    .assertAllItemsDisplayedInOrder(dst);

        } finally {
            deleteRecursiveIfExists(dir);
        }
    }

    @Test
    public void can_start_from_data_uri() throws Exception {
        Path dir = createDirs(dir().resolve("dir"));
        Path file = createFile(dir.resolve("file"));
        setActivityIntent(new Intent().setData(Uri.parse(dir.toUri().toString())));
        screen()
                .assertCurrentDirectory(dir)
                .assertListViewContains(file, true);
    }

    @Test
    public void can_preview() throws Exception {
        Path empty = createFile(dir().resolve("empty"));
        Path file = dir().resolve("file");
        Path link = createLink(dir().resolve("link"), file);
        writeUtf8(file, "hello");
        screen()
                .assertThumbnailShown(file, true)
                .assertThumbnailShown(link, true)
                .assertThumbnailShown(empty, false);
    }

    @Test
    public void can_navigate_into_non_utf8_named_dir() throws Exception {

        byte[] nonUtf8Name = {-19, -96, -67, -19, -80, -117};
        assertNotEquals(
                nonUtf8Name.clone(),
                new String(nonUtf8Name.clone(), UTF_8).getBytes(UTF_8)
        );

        Path nonUtf8NamedDir = createDir(dir().resolve(nonUtf8Name));
        Path child = createFile(nonUtf8NamedDir.resolve("a"));

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

        Path notUtf8Dir = createDir(dir().resolve(notUtf8));
        Path notUtf8Child = createFile(notUtf8Dir.resolve("notUtf8"));

        Path utf8Dir = createDir(dir().resolve(utf8));
        Path utf8Child = createFile(utf8Dir.resolve("utf8"));

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

        screen().selectFromNavigationMode(LocalPath.of("/"));
        screen().clickInto(LocalPath.of("/proc"));
        screen().clickInto(LocalPath.of("/proc/self"));
        screen().clickInto(LocalPath.of("/proc/self/fdinfo"));
    }

    @Test
    public void can_navigate_through_title_list_drop_down() throws Exception {
        Path parent = dir().parent();
        screen()
                .selectFromNavigationMode(parent)
                .assertNavigationModeHierarchy(parent);
    }

    @Test
    public void updates_navigation_list_when_going_into_a_new_dir() throws Exception {
        screen().assertNavigationModeHierarchy(dir());
        Path dir = createDir(dir().resolve("dir"));
        screen().clickInto(dir).assertNavigationModeHierarchy(dir);
    }

    @Test
    public void shows_size_only_if_unable_to_determine_modified_date() throws Exception {
        Path file = createFile(dir().resolve("file"));
        setLastModifiedTime(file, NOFOLLOW, EPOCH);

        long size = stat(file, NOFOLLOW).size();
        Context c = getActivity();
        final String expected = formatShortFileSize(c, size);

        screen().assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView view) {
                assertTrue(view.getSummary().getText().toString().contains(expected));
            }
        });
    }

    @Test
    public void shows_time_and_size_for_file() throws Exception {
        Path file = createFile(dir().resolve("file"));
        appendUtf8(file, file.toString());

        Context c = getActivity();
        String date = getTimeFormat(c).format(new Date());
        String size = formatShortFileSize(c, stat(file, NOFOLLOW).size());
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
        Path d = createDir(dir().resolve("dir"));
        setLastModifiedTime(d, NOFOLLOW, Instant.of(modifiedAt / 1000, 0));
        screen().assertSummary(d, expected);
    }

    @Test
    public void directory_view_is_disabled_if_no_read_permission() throws Exception {
        Path dir = createDir(dir().resolve("dir"));
        removePermissions(dir, Permission.read());
        screen().assertDisabled(dir);
    }

    @Test
    public void link_displayed() throws Exception {
        Path dir = createDir(dir().resolve("dir"));
        Path link = createLink(dir().resolve("link"), dir);

        screen()
                .assertLinkIconDisplayed(dir, false)
                .assertLinkIconDisplayed(link, true)
                .assertLinkPathDisplayed(dir, null)
                .assertLinkPathDisplayed(link, dir);
    }

    @Test
    public void can_see_changes_in_linked_directory() throws Exception {
        Path dir = createDir(dir().resolve("dir"));
        Path link = createLink(dir().resolve("link"), dir);
        screen()
                .clickInto(link)
                .assertCurrentDirectory(link);

        Path child = createDir(link.resolve("child"));
        screen()
                .clickInto(child)
                .assertCurrentDirectory(child);
    }

    @Test
    public void press_action_bar_up_indicator_will_go_back() throws Exception {
        Path dir = createDir(dir().resolve("dir"));
        screen()
                .clickInto(dir)
                .assertCurrentDirectory(dir)
                .pressActionBarUpIndicator()
                .assertCurrentDirectory(dir.parent());
    }

    @Test
    public void action_bar_title_shows_name_of_directory() throws Exception {
        screen()
                .clickInto(createDir(dir().resolve("a")))
                .assertActionBarTitle("a");
    }

    @Test
    public void action_bar_hides_up_indicator_when_there_is_no_back_stack_initially() {
        screen().assertActionBarUpIndicatorIsVisible(false);
    }

    @Test
    public void action_bar_shows_up_indicator_when_there_is_back_stack() throws Exception {
        screen()
                .clickInto(createDir(dir().resolve("dir")))
                .assertActionBarUpIndicatorIsVisible(true);
    }

    @Test
    public void action_bar_hides_up_indicator_when_there_is_no_back_stack_to_go_back_to() throws Exception {
        screen()
                .clickInto(createDir(dir().resolve("dir")))
                .pressBack()
                .assertActionBarUpIndicatorIsVisible(false);
    }

    @Test
    public void long_press_back_will_clear_back_stack() throws Exception {
        screen()
                .clickInto(createDir(dir().resolve("a")))
                .clickInto(createDir(dir().resolve("a/b")))
                .clickInto(createDir(dir().resolve("a/b/c")))
                .longPressBack()
                .assertCurrentDirectory(dir());
    }

    @Test
    public void observes_on_current_directory_and_shows_added_deleted_files() throws Exception {
        Path a = createDir(dir().resolve("a"));
        screen().assertListViewContains(a, true);

        Path b = createFile(dir().resolve("b"));
        screen()
                .assertListViewContains(a, true)
                .assertListViewContains(b, true);

        delete(b);
        screen()
                .assertListViewContains(a, true)
                .assertListViewContains(b, false);
    }

    @Test
    public void updates_view_on_child_directory_modified() throws Exception {
        Path dir = createDir(dir().resolve("a"));
        testUpdatesDateViewOnChildModified(dir);
    }

    @Test
    public void updates_view_on_child_file_modified() throws Exception {
        Path file = createFile(dir().resolve("a"));
        testUpdatesDateViewOnChildModified(file);
        testUpdatesSizeViewOnChildModified(file);
    }

    private void testUpdatesSizeViewOnChildModified(Path file)
            throws IOException {
        setLastModifiedTime(file, NOFOLLOW, EPOCH);

        final CharSequence[] chars = {null};
        screen().assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView input) {
                chars[0] = input.getSummary().getText();
            }
        });

        modify(file);

        screen().assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView input) {
                assertNotEqual(chars[0], input.getSummary().getText());
            }
        });
    }

    private void testUpdatesDateViewOnChildModified(Path file)
            throws IOException {

        setLastModifiedTime(file, NOFOLLOW, Instant.of(100000, 1));

        final CharSequence[] date = {null};
        screen().assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView input) {
                date[0] = input.getSummary().getText();
            }
        });

        modify(file);

        screen().assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView input) {
                assertNotEqual(date[0], input.getSummary().getText());
            }
        });
    }

    private Path modify(Path file) throws IOException {
        Stat stat = stat(file, NOFOLLOW);
        Instant lastModifiedBefore = stat.lastModifiedTime();
        if (stat.isDirectory()) {
            createDir(file.resolve(String.valueOf(nanoTime())));
        } else {
            appendUtf8(file, "test");
        }
        Instant lastModifiedAfter = stat(file, NOFOLLOW).lastModifiedTime();
        assertNotEqual(lastModifiedBefore, lastModifiedAfter);
        return file;
    }

}
