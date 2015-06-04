package l.files.features;

import android.content.Context;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.fs.Instant;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.test.BaseFilesActivityTest;

import static android.test.MoreAsserts.assertNotEqual;
import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_NO_YEAR;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.formatDateTime;
import static android.text.format.Formatter.formatShortFileSize;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.DAYS;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class NavigationTest extends BaseFilesActivityTest
{

    public void test_shows_size_only_if_unable_to_determine_modified_date()
            throws Exception
    {
        final Resource file = directory().resolve("file").createFile();
        file.setModificationTime(NOFOLLOW, EPOCH);

        final long size = file.stat(NOFOLLOW).size();
        final Context c = getActivity();
        final String expected = formatShortFileSize(c, size);

        screen().assertSummaryView(file, new Consumer<CharSequence>()
        {
            @Override
            public void apply(final CharSequence actual)
            {
                assertEquals(expected, actual);
            }
        });
    }

    public void test_shows_time_and_size_for_file() throws Exception
    {
        final Resource file = directory().resolve("file").createFile();
        file.writeString(NOFOLLOW, UTF_8, file.path());

        final Context c = getActivity();
        final String date = getTimeFormat(c).format(new Date());
        final String size = formatShortFileSize(c, file.stat(NOFOLLOW).size());
        final String expected = c.getString(R.string.x_dot_y, date, size);

        screen().assertSummaryView(file, new Consumer<CharSequence>()
        {
            @Override
            public void apply(final CharSequence actual)
            {
                assertEquals(expected, actual);
            }
        });
    }

    public void test_shows_time_only_for_today() throws Exception
    {
        final long time = currentTimeMillis();
        final DateFormat format = getTimeFormat(getActivity());
        final String expected = format.format(new Date(time));
        testDirectorySummary(expected, time);
    }

    public void test_shows_time_as_month_day_for_date_of_current_year()
            throws Exception
    {
        final long time = currentTimeMillis() - DAYS.toMillis(2);
        final int flags
                = FORMAT_SHOW_DATE
                | FORMAT_ABBREV_MONTH
                | FORMAT_NO_YEAR;
        final String expected = formatDateTime(getActivity(), time, flags);
        testDirectorySummary(expected, time);
    }

    public void test_shows_time_as_year_month_day_for_date_outside_of_current_year()
            throws Exception
    {
        final long time = currentTimeMillis() - DAYS.toMillis(400);
        final DateFormat format = getDateFormat(getActivity());
        final String expected = format.format(new Date(time));
        testDirectorySummary(expected, time);
    }

    private void testDirectorySummary(
            final String expected,
            final long modifiedAt) throws Exception
    {
        final Resource d = directory().resolve("dir").createDirectory();
        d.setModificationTime(NOFOLLOW, Instant.of(modifiedAt / 1000, 0));
        screen().assertSummaryView(d, new Consumer<CharSequence>()
        {
            @Override
            public void apply(final CharSequence actual)
            {
                assertEquals(expected, actual);
            }
        });
    }

    public void test_directory_view_is_disabled_if_no_read_permission()
            throws Exception
    {
        final Resource dir = directory().resolve("dir").createDirectory();
        dir.removePermissions(Permission.read());
        screen().assertDisabled(dir);
    }

    public void test_link_icon_displayed() throws Exception
    {
        final Resource dir = directory().resolve("dir").createDirectory();
        final Resource link = directory().resolve("link").createLink(dir);

        screen()
                .assertSymbolicLinkIconDisplayed(dir, false)
                .assertSymbolicLinkIconDisplayed(link, true);
    }

    public void test_can_navigate_into_linked_directory() throws Exception
    {
        final Resource dir = directory().resolve("dir").createDirectory();
        dir.resolve("a").createDirectory();

        final Resource link = directory().resolve("link").createLink(dir);
        final Resource linkChild = link.resolve("a");
        screen()
                .selectItem(link)
                .selectItem(linkChild)
                .assertCurrentDirectory(linkChild);
    }

    public void test_can_see_changes_in_linked_directory() throws Exception
    {
        final Resource dir = directory().resolve("dir").createDirectory();
        final Resource link = directory().resolve("link").createLink(dir);
        screen().selectItem(link)
                .assertCurrentDirectory(link);

        final Resource child = link.resolve("child").createDirectory();
        screen().selectItem(child)
                .assertCurrentDirectory(child);
    }

    public void test_press_action_bar_up_indicator_will_go_back()
            throws Exception
    {
        final Resource dir = directory().resolve("dir").createDirectory();
        screen()
                .selectItem(dir)
                .assertCurrentDirectory(dir)
                .pressActionBarUpIndicator()
                .assertCurrentDirectory(dir.parent());
    }

    public void test_action_bar_title_shows_name_of_directory()
            throws Exception
    {
        screen()
                .selectItem(directory().resolve("a").createDirectory())
                .assertActionBarTitle("a");
    }

    public void test_action_bar_hides_up_indicator_when_there_is_no_back_stack_initially()
    {
        screen().assertActionBarUpIndicatorIsVisible(false);
    }

    public void test_action_bar_shows_up_indicator_when_there_is_back_stack()
            throws Exception
    {
        screen()
                .selectItem(directory().resolve("dir").createDirectory())
                .assertActionBarUpIndicatorIsVisible(true);
    }

    public void test_action_bar_hides_up_indicator_when_there_is_no_back_stack_to_go_back_to()
            throws Exception
    {
        screen()
                .selectItem(directory().resolve("dir").createDirectory())
                .pressBack()
                .assertActionBarUpIndicatorIsVisible(false);
    }

    public void test_long_press_back_will_clear_back_stack() throws Exception
    {
        screen()
                .selectItem(directory().resolve("a").createDirectory())
                .selectItem(directory().resolve("a/b").createDirectory())
                .selectItem(directory().resolve("a/b/c").createDirectory())
                .longPressBack()
                .assertCurrentDirectory(directory());
    }

    public void test_open_new_directory_will_close_opened_drawer()
            throws Exception
    {
        final Resource dir = directory().resolve("a").createDirectory();
        screen()
                .openBookmarksDrawer()
                .getActivityObject()
                .selectItem(dir)
                .assertDrawerIsOpened(false);
    }

    public void test_observes_on_current_directory_and_shows_newly_added_files()
            throws Exception
    {
        final Resource dir = directory().resolve("a").createDirectory();
        screen().assertListViewContains(dir, true);
    }

    public void test_observes_on_current_directory_and_hides_deleted_files()
            throws Exception
    {
        final Resource file = directory().resolve("a").createFile();
        screen().assertListViewContains(file, true);
        file.delete();
        screen().assertListViewContains(file, false);
    }

    public void test_updates_view_on_child_directory_modified()
            throws Exception
    {
        final Resource dir = directory().resolve("a").createDirectory();
        testUpdatesDateViewOnChildModified(dir);
    }

    public void test_updates_view_on_child_file_modified() throws Exception
    {
        final Resource file = directory().resolve("a").createFile();
        testUpdatesDateViewOnChildModified(file);
        testUpdatesSizeViewOnChildModified(file);
    }

    private void testUpdatesSizeViewOnChildModified(final Resource resource)
            throws IOException
    {
        resource.setModificationTime(NOFOLLOW, EPOCH);

        final CharSequence[] size = {null};
        screen().assertSummaryView(resource, new Consumer<CharSequence>()
        {
            @Override
            public void apply(final CharSequence input)
            {
                assertFalse(isNullOrEmpty(input.toString()));
                size[0] = input;
            }
        });

        modify(resource);

        screen().assertSummaryView(resource, new Consumer<CharSequence>()
        {
            @Override
            public void apply(final CharSequence input)
            {
                assertNotEqual(size[0], input);
            }
        });
    }

    private void testUpdatesDateViewOnChildModified(final Resource resource)
            throws IOException
    {
        resource.setModificationTime(NOFOLLOW, Instant.of(1, 1));

        final CharSequence[] date = {null};
        screen().assertSummaryView(resource, new Consumer<CharSequence>()
        {
            @Override
            public void apply(final CharSequence input)
            {
                assertFalse(isNullOrEmpty(input.toString()));
                date[0] = input;
            }
        });

        modify(resource);

        screen().assertSummaryView(resource, new Consumer<CharSequence>()
        {
            @Override
            public void apply(final CharSequence input)
            {
                assertNotEqual(date[0], input);
            }
        });
    }

    private Resource modify(final Resource resource) throws IOException
    {
        final Stat stat = resource.stat(NOFOLLOW);
        final Instant lastModifiedBefore = stat.modificationTime();
        if (stat.isDirectory())
        {
            resource.resolve(String.valueOf(nanoTime())).createDirectory();
        }
        else
        {
            try (Writer writer = resource.writer(NOFOLLOW, UTF_8, true))
            {
                writer.write("test");
            }
        }
        final Instant lastModifiedAfter = resource.stat(NOFOLLOW).modificationTime();
        assertNotEqual(lastModifiedBefore, lastModifiedAfter);
        return resource;
    }

}
