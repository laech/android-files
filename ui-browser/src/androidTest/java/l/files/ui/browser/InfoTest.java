package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.testing.fs.ExtendedPath;

import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.formatDateTime;
import static android.text.format.Formatter.formatFileSize;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.NOFOLLOW;

@RunWith(AndroidJUnit4.class)
public final class InfoTest extends BaseFilesActivityTest {

    @Test
    public void gets_info_of_multiple_files() throws Exception {

        Path child1 = dir().concat("1").createFile();
        Path child2 = dir().concat("2").createDir();
        Path child3 = dir().concat("3").createSymbolicLink(child2);

        Stat st1 = child1.stat(NOFOLLOW);
        Stat st2 = child2.stat(NOFOLLOW);
        Stat st3 = child3.stat(NOFOLLOW);

        screen()
                .longClick(child1)
                .click(child2)
                .click(child3)
                .getInfo()
                .assertSize(formatSizeCount(
                        st1.size()
                                + st2.size()
                                + st3.size(),
                        3))
                .assertSizeOnDisk(formatSizeOnDisk(
                        st1.sizeOnDisk()
                                + st2.sizeOnDisk()
                                + st3.sizeOnDisk()));
    }

    @Test
    public void gets_info_of_file() throws Exception {

        ExtendedPath path = dir().concat("test.txt");
        path.writeUtf8("hello world");
        Stat stat = path.stat(NOFOLLOW);

        screen()
                .longClick(path)
                .getInfo()
                .assertName(path.name().toString())
                .assertDate(formatDate(stat))
                .assertSize(formatSize(stat.size()))
                .assertSizeOnDisk(formatSizeOnDisk(stat.sizeOnDisk()));
    }

    @Test
    public void gets_info_of_link() throws Exception {

        dir().setLastModifiedTime(NOFOLLOW, EPOCH);
        Path path = dir().concat("link").createSymbolicLink(dir());
        Stat stat = path.stat(NOFOLLOW);

        screen()
                .longClick(path)
                .getInfo()
                .assertName(path.name().toString())
                .assertDate(formatDate(stat))
                .assertSize(formatSize(stat.size()))
                .assertSizeOnDisk(formatSizeOnDisk(stat.sizeOnDisk()));
    }

    @Test
    public void gets_info_of_empty_dir() throws Exception {

        Path dir = dir().concat("dir").createDir();
        Stat stat = dir.stat(NOFOLLOW);

        screen()
                .longClick(dir)
                .getInfo()
                .assertName(dir.name().toString())
                .assertDate(formatDate(stat))
                .assertSize(formatSizeCount(stat.size(), 1))
                .assertSizeOnDisk(formatSizeOnDisk(stat.sizeOnDisk()));
    }

    @Test
    public void gets_info_of_non_empty_dir() throws Exception {

        Path dir = dir().concat("dir").createDir();
        Path child1 = dir.concat("dir").createDir();
        Path child2 = dir.concat("file").createFile();
        Path child3 = dir.concat("link").createSymbolicLink(dir);

        Stat stat = dir.stat(NOFOLLOW);
        Stat childStat1 = child1.stat(NOFOLLOW);
        Stat childStat2 = child2.stat(NOFOLLOW);
        Stat childStat3 = child3.stat(NOFOLLOW);

        screen()
                .longClick(dir)
                .getInfo()
                .assertName(dir.name().toString())
                .assertDate(formatDate(stat))
                .assertSize(formatSizeCount(
                        stat.size()
                                + childStat1.size()
                                + childStat2.size()
                                + childStat3.size(),
                        4))
                .assertSizeOnDisk(formatSizeOnDisk(
                        stat.sizeOnDisk()
                                + childStat1.sizeOnDisk()
                                + childStat2.sizeOnDisk()
                                + childStat3.sizeOnDisk()));
    }

    private String formatDate(Stat stat) {
        long millis = stat.lastModifiedTime().to(MILLISECONDS);
        int flags = FORMAT_SHOW_DATE | FORMAT_SHOW_TIME;
        return formatDateTime(getActivity(), millis, flags);
    }


    private String formatSize(long size) {
        return formatFileSize(getActivity(), size);
    }

    private String formatSizeOnDisk(long sizeOnDisk) {
        return getActivity().getString(
                l.files.ui.info.R.string.x_size_on_disk,
                formatSize(sizeOnDisk));
    }

    private String formatSizeCount(long size, int count) {
        return getActivity().getResources().getQuantityString(
                l.files.ui.info.R.plurals.x_size_y_items,
                count,
                formatSize(size),
                count);
    }

}
