package l.files.ui.browser;

import android.content.Context;

import org.junit.Test;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Permission;
import l.files.fs.Stat;

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
import static l.files.fs.File.UTF_8;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public final class NavigationTest extends BaseFilesActivityTest {

    @Test
    public void can_preview() throws Exception {
        File empty = dir().resolve("empty").createFile();
        File file = dir().resolve("file");
        File link = dir().resolve("link").createLink(file);
        file.writeAllUtf8("hello");
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

        File nonUtf8NamedDir = dir().resolve(nonUtf8Name).createDir();
        File child = nonUtf8NamedDir.resolve("a").createFile();

        screen()
                .clickInto(nonUtf8NamedDir)
                .assertListViewContains(child, true);
    }

    @Test
    public void can_navigate_into_etc_proc_self_fdinfo_without_crashing()
            throws Exception {

        screen().selectFromNavigationMode(dir().resolve("/"));
        screen().clickInto(dir().resolve("/proc"));
        screen().clickInto(dir().resolve("/proc/self"));
        screen().clickInto(dir().resolve("/proc/self/fdinfo"));
    }

    @Test
    public void can_navigate_through_title_list_drop_down() throws Exception {
        File parent = dir().parent();
        screen()
                .selectFromNavigationMode(parent)
                .assertNavigationModeHierarchy(parent);
    }

    @Test
    public void updates_navigation_list_when_going_into_a_new_dir() throws Exception {
        screen().assertNavigationModeHierarchy(dir());
        File dir = dir().resolve("dir").createDir();
        screen().clickInto(dir).assertNavigationModeHierarchy(dir);
    }

    @Test
    public void shows_size_only_if_unable_to_determine_modified_date() throws Exception {
        File file = dir().resolve("file").createFile();
        file.setLastModifiedTime(NOFOLLOW, EPOCH);

        long size = file.stat(NOFOLLOW).size();
        Context c = getActivity();
        final String expected = formatShortFileSize(c, size);

        screen().assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView view) {
                assertTrue(view.getText().toString().contains(expected));
            }
        });
    }

    @Test
    public void shows_time_and_size_for_file() throws Exception {
        File file = dir().resolve("file").createFile();
        file.appendUtf8(file.path().toString());

        Context c = getActivity();
        String date = getTimeFormat(c).format(new Date());
        String size = formatShortFileSize(c, file.stat(NOFOLLOW).size());
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
        File d = dir().resolve("dir").createDir();
        d.setLastModifiedTime(NOFOLLOW, Instant.of(modifiedAt / 1000, 0));
        screen().assertSummary(d, expected);
    }

    @Test
    public void directory_view_is_disabled_if_no_read_permission() throws Exception {
        File dir = dir().resolve("dir").createDir();
        dir.removePermissions(Permission.read());
        screen().assertDisabled(dir);
    }

    @Test
    public void link_displayed() throws Exception {
        File dir = dir().resolve("dir").createDir();
        File link = dir().resolve("link").createLink(dir);

        screen()
                .assertLinkIconDisplayed(dir, false)
                .assertLinkIconDisplayed(link, true)
                .assertLinkPathDisplayed(dir, null)
                .assertLinkPathDisplayed(link, dir);
    }

    @Test
    public void can_see_changes_in_linked_directory() throws Exception {
        File dir = dir().resolve("dir").createDir();
        File link = dir().resolve("link").createLink(dir);
        screen()
                .clickInto(link)
                .assertCurrentDirectory(link);

        File child = link.resolve("child").createDir();
        screen()
                .clickInto(child)
                .assertCurrentDirectory(child);
    }

    @Test
    public void press_action_bar_up_indicator_will_go_back() throws Exception {
        File dir = dir().resolve("dir").createDir();
        screen()
                .clickInto(dir)
                .assertCurrentDirectory(dir)
                .pressActionBarUpIndicator()
                .assertCurrentDirectory(dir.parent());
    }

    @Test
    public void action_bar_title_shows_name_of_directory() throws Exception {
        screen()
                .clickInto(dir().resolve("a").createDir())
                .assertActionBarTitle("a");
    }

    @Test
    public void action_bar_hides_up_indicator_when_there_is_no_back_stack_initially() {
        screen().assertActionBarUpIndicatorIsVisible(false);
    }

    @Test
    public void action_bar_shows_up_indicator_when_there_is_back_stack() throws Exception {
        screen()
                .clickInto(dir().resolve("dir").createDir())
                .assertActionBarUpIndicatorIsVisible(true);
    }

    @Test
    public void action_bar_hides_up_indicator_when_there_is_no_back_stack_to_go_back_to() throws Exception {
        screen()
                .clickInto(dir().resolve("dir").createDir())
                .pressBack()
                .assertActionBarUpIndicatorIsVisible(false);
    }

    @Test
    public void long_press_back_will_clear_back_stack() throws Exception {
        screen()
                .clickInto(dir().resolve("a").createDir())
                .clickInto(dir().resolve("a/b").createDir())
                .clickInto(dir().resolve("a/b/c").createDir())
                .longPressBack()
                .assertCurrentDirectory(dir());
    }

    @Test
    public void observes_on_current_directory_and_shows_added_deleted_files() throws Exception {
        File a = dir().resolve("a").createDir();
        screen().assertListViewContains(a, true);

        File b = dir().resolve("b").createFile();
        screen()
                .assertListViewContains(a, true)
                .assertListViewContains(b, true);

        b.delete();
        screen()
                .assertListViewContains(a, true)
                .assertListViewContains(b, false);
    }

    @Test
    public void updates_view_on_child_directory_modified() throws Exception {
        File dir = dir().resolve("a").createDir();
        testUpdatesDateViewOnChildModified(dir);
    }

    @Test
    public void updates_view_on_child_file_modified() throws Exception {
        File file = dir().resolve("a").createFile();
        testUpdatesDateViewOnChildModified(file);
        testUpdatesSizeViewOnChildModified(file);
    }

    private void testUpdatesSizeViewOnChildModified(File file)
            throws IOException {
        file.setLastModifiedTime(NOFOLLOW, EPOCH);

        final String[] chars = {null};
        screen().assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView input) {
                chars[0] = input.getText().toString();
            }
        });

        modify(file);

        screen().assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView input) {
                assertNotEqual(chars[0], input.getText().toString());
            }
        });
    }

    private void testUpdatesDateViewOnChildModified(File file)
            throws IOException {
        file.setLastModifiedTime(NOFOLLOW, Instant.of(100000, 1));

        final String[] date = {null};
        screen().assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView input) {
                date[0] = input.getText().toString();
            }
        });

        modify(file);

        screen().assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView input) {
                assertNotEqual(date[0], input.getText().toString());
            }
        });
    }

    private File modify(File file) throws IOException {
        Stat stat = file.stat(NOFOLLOW);
        Instant lastModifiedBefore = stat.lastModifiedTime();
        if (stat.isDirectory()) {
            file.resolve(String.valueOf(nanoTime())).createDir();
        } else {
            file.appendUtf8("test");
        }
        Instant lastModifiedAfter = file.stat(NOFOLLOW).lastModifiedTime();
        assertNotEqual(lastModifiedBefore, lastModifiedAfter);
        return file;
    }

}
