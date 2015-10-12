package l.files.features;

import android.content.Context;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Permission;
import l.files.fs.Stat;
import l.files.fs.Stream;
import l.files.test.BaseFilesActivityTest;

import static android.test.MoreAsserts.assertNotEqual;
import static android.text.TextUtils.isEmpty;
import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_NO_YEAR;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.formatDateTime;
import static android.text.format.Formatter.formatShortFileSize;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.getDateTimeInstance;
import static java.util.concurrent.TimeUnit.DAYS;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class NavigationTest extends BaseFilesActivityTest {

    // TODO can show /etc/proc/self/fdinfo without crashing with StackOverflowError

    public void test_can_start_action_mode_after_rotation() throws Exception {
        for (int i = 0; i < 10; i++) {
            dir().resolve(String.valueOf(i)).createFile();
        }

        try (Stream<File> stream = dir().list(NOFOLLOW)) {
            File child = stream.iterator().next();
            screen()
                    .rotate()
                    .longClick(child)
                    .assertChecked(child, true)
                    .assertActionModePresent(true);
        }
    }

    public void test_clears_selection_on_finish_of_action_mode() throws Exception {
        File a = dir().resolve("a").createFile();
        screen()
                .longClick(a)
                .assertActionModePresent(true)
                .assertChecked(a, true)
                .assertActionModeTitle(1)

                .pressBack()
                .assertActionModePresent(false)
                .assertChecked(a, false)

                .rotate()
                .assertActionModePresent(false)
                .assertChecked(a, false);
    }

    public void test_maintains_action_mode_on_screen_rotation() throws Exception {
        File a = dir().resolve("a").createFile();
        File b = dir().resolve("b").createFile();
        screen()
                .longClick(a)
                .assertActionModePresent(true)
                .assertActionModeTitle(1)

                .rotate()
                .assertActionModePresent(true)
                .assertActionModeTitle(1)
                .assertChecked(a, true)
                .assertChecked(b, false)

                .click(b)
                .assertActionModePresent(true)
                .assertActionModeTitle(2);
    }

    public void test_can_navigate_through_title_list_drop_down() throws Exception {
        File parent = dir().parent();
        screen()
                .selectFromNavigationMode(parent)
                .assertNavigationModeHierarchy(parent);
    }

    public void test_updates_navigation_list_when_going_into_a_new_dir() throws Exception {
        File dir = dir().resolve("dir").createDir();
        screen().clickInto(dir).assertNavigationModeHierarchy(dir);
    }

    public void test_shows_initial_navigation_list() throws Exception {
        screen().assertNavigationModeHierarchy(dir());
    }

    public void test_shows_size_only_if_unable_to_determine_modified_date() throws Exception {
        File file = dir().resolve("file").createFile();
        file.setLastModifiedTime(NOFOLLOW, EPOCH);

        long size = file.stat(NOFOLLOW).size();
        Context c = getActivity();
        final String expected = formatShortFileSize(c, size);

        screen().assertSummaryView(file, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence actual) {
                assertEquals(expected, actual);
            }
        });
    }

    public void test_shows_full_time_for_future_file() throws Exception {
        File file = dir().resolve("file").createFile();
        long future = currentTimeMillis() + 100000;
        file.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(future));

        final String date = getDateTimeInstance(MEDIUM, MEDIUM)
                .format(new Date(future));

        screen().assertSummaryView(file, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence actual) {
                assertTrue(
                        String.format("\"%s\".startsWith(\"%s\")", actual, date),
                        actual.toString().startsWith(date));
            }
        });
    }

    public void test_shows_time_and_size_for_file() throws Exception {
        File file = dir().resolve("file").createFile();
        file.appendUtf8(file.path());

        Context c = getActivity();
        String date = getTimeFormat(c).format(new Date());
        String size = formatShortFileSize(c, file.stat(NOFOLLOW).size());
        final String expected = c.getString(R.string.x_dot_y, date, size);

        screen().assertSummaryView(file, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence actual) {
                assertEquals(expected, actual);
            }
        });
    }

    public void test_shows_time_only_for_today() throws Exception {
        long time = currentTimeMillis();
        DateFormat format = getTimeFormat(getActivity());
        String expected = format.format(new Date(time));
        testDirectorySummary(expected, time);
    }

    public void test_shows_time_as_month_day_for_date_of_current_year() throws Exception {
        long time = currentTimeMillis() - DAYS.toMillis(2);
        int flags
                = FORMAT_SHOW_DATE
                | FORMAT_ABBREV_MONTH
                | FORMAT_NO_YEAR;
        String expected = formatDateTime(getActivity(), time, flags);
        testDirectorySummary(expected, time);
    }

    public void test_shows_time_as_year_month_day_for_date_outside_of_current_year() throws Exception {
        long time = currentTimeMillis() - DAYS.toMillis(400);
        DateFormat format = getDateFormat(getActivity());
        String expected = format.format(new Date(time));
        testDirectorySummary(expected, time);
    }

    private void testDirectorySummary(
            final String expected, long modifiedAt) throws Exception {
        File d = dir().resolve("dir").createDir();
        d.setLastModifiedTime(NOFOLLOW, Instant.of(modifiedAt / 1000, 0));
        screen().assertSummaryView(d, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence actual) {
                assertEquals(expected, actual);
            }
        });
    }

    public void test_directory_view_is_disabled_if_no_read_permission() throws Exception {
        File dir = dir().resolve("dir").createDir();
        dir.removePermissions(Permission.read());
        screen().assertDisabled(dir);
    }

    public void test_link_icon_displayed() throws Exception {
        File dir = dir().resolve("dir").createDir();
        File link = dir().resolve("link").createLink(dir);

        screen()
                .assertSymbolicLinkIconDisplayed(dir, false)
                .assertSymbolicLinkIconDisplayed(link, true);
    }

    public void test_can_navigate_into_linked_directory() throws Exception {
        File dir = dir().resolve("dir").createDir();
        dir.resolve("a").createDir();

        File link = dir().resolve("link").createLink(dir);
        File linkChild = link.resolve("a");
        screen()
                .clickInto(link)
                .clickInto(linkChild)
                .assertCurrentDirectory(linkChild);
    }

    public void test_can_see_changes_in_linked_directory() throws Exception {
        File dir = dir().resolve("dir").createDir();
        File link = dir().resolve("link").createLink(dir);
        screen().clickInto(link)
                .assertCurrentDirectory(link);

        File child = link.resolve("child").createDir();
        screen().clickInto(child)
                .assertCurrentDirectory(child);
    }

    public void test_press_action_bar_up_indicator_will_go_back() throws Exception {
        File dir = dir().resolve("dir").createDir();
        screen()
                .clickInto(dir)
                .assertCurrentDirectory(dir)
                .pressActionBarUpIndicator()
                .assertCurrentDirectory(dir.parent());
    }

    public void test_action_bar_title_shows_name_of_directory() throws Exception {
        screen()
                .clickInto(dir().resolve("a").createDir())
                .assertActionBarTitle("a");
    }

    public void test_action_bar_hides_up_indicator_when_there_is_no_back_stack_initially() {
        screen().assertActionBarUpIndicatorIsVisible(false);
    }

    public void test_action_bar_shows_up_indicator_when_there_is_back_stack() throws Exception {
        screen()
                .clickInto(dir().resolve("dir").createDir())
                .assertActionBarUpIndicatorIsVisible(true);
    }

    public void test_action_bar_hides_up_indicator_when_there_is_no_back_stack_to_go_back_to() throws Exception {
        screen()
                .clickInto(dir().resolve("dir").createDir())
                .pressBack()
                .assertActionBarUpIndicatorIsVisible(false);
    }

    public void test_long_press_back_will_clear_back_stack() throws Exception {
        screen()
                .clickInto(dir().resolve("a").createDir())
                .clickInto(dir().resolve("a/b").createDir())
                .clickInto(dir().resolve("a/b/c").createDir())
                .longPressBack()
                .assertCurrentDirectory(dir());
    }

    public void test_open_new_directory_will_close_opened_drawer() throws Exception {
        File dir = dir().resolve("a").createDir();
        screen()
                .openBookmarksDrawer()
                .activityObject()
                .clickInto(dir)
                .assertDrawerIsOpened(false);
    }

    public void test_observes_on_current_directory_and_shows_newly_added_files() throws Exception {
        File dir = dir().resolve("a").createDir();
        screen().assertListViewContains(dir, true);
    }

    public void test_observes_on_current_directory_and_hides_deleted_files() throws Exception {
        File file = dir().resolve("a").createFile();
        screen().assertListViewContains(file, true);
        file.delete();
        screen().assertListViewContains(file, false);
    }

    public void test_updates_view_on_child_directory_modified() throws Exception {
        File dir = dir().resolve("a").createDir();
        testUpdatesDateViewOnChildModified(dir);
    }

    public void test_updates_view_on_child_file_modified() throws Exception {
        File file = dir().resolve("a").createFile();
        testUpdatesDateViewOnChildModified(file);
        testUpdatesSizeViewOnChildModified(file);
    }

    private void testUpdatesSizeViewOnChildModified(File file)
            throws IOException {
        file.setLastModifiedTime(NOFOLLOW, EPOCH);

        final CharSequence[] size = {null};
        screen().assertSummaryView(file, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence input) {
                assertFalse(isEmpty(input.toString()));
                size[0] = input;
            }
        });

        modify(file);

        screen().assertSummaryView(file, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence input) {
                assertNotEqual(size[0], input);
            }
        });
    }

    private void testUpdatesDateViewOnChildModified(File file)
            throws IOException {
        file.setLastModifiedTime(NOFOLLOW, Instant.of(100000, 1));

        final String[] date = {null};
        screen().assertSummaryView(file, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence input) {
                assertFalse(isEmpty(input.toString()));
                date[0] = input.toString();
            }
        });

        modify(file);

        screen().assertSummaryView(file, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence input) {
                assertNotEqual(date[0], input.toString());
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
