package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import l.files.fs.Path;
import l.files.fs.Stat;

import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.formatDateTime;
import static android.text.format.Formatter.formatFileSize;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.testing.fs.Files.writeUtf8;

@RunWith(AndroidJUnit4.class)
public final class InfoTest extends BaseFilesActivityTest {

    @Test
    public void gets_info_of_multiple_files() throws Exception {

        Path child1 = fs.createFile(dir().concat("1"));
        Path child2 = fs.createDir(dir().concat("2"));
        Path child3 = fs.createSymbolicLink(dir().concat("3"), child2);

        Stat st1 = fs.stat(child1, NOFOLLOW);
        Stat st2 = fs.stat(child2, NOFOLLOW);
        Stat st3 = fs.stat(child3, NOFOLLOW);

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

        Path path = dir().concat("test.txt");
        writeUtf8(fs, path, "hello world");
        Stat stat = fs.stat(path, NOFOLLOW);

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

        fs.setLastModifiedTime(dir(), NOFOLLOW, EPOCH);
        Path path = fs.createSymbolicLink(dir().concat("link"), dir());
        Stat stat = fs.stat(path, NOFOLLOW);

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

        Path dir = fs.createDir(dir().concat("dir"));
        Stat stat = fs.stat(dir, NOFOLLOW);

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

        Path dir = fs.createDir(dir().concat("dir"));
        Path child1 = fs.createDir(dir.concat("dir"));
        Path child2 = fs.createFile(dir.concat("file"));
        Path child3 = fs.createSymbolicLink(dir.concat("link"), dir);

        Stat stat = fs.stat(dir, NOFOLLOW);
        Stat childStat1 = fs.stat(child1, NOFOLLOW);
        Stat childStat2 = fs.stat(child2, NOFOLLOW);
        Stat childStat3 = fs.stat(child3, NOFOLLOW);

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
