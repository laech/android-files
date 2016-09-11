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
import static l.files.fs.Files.createDir;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.createSymbolicLink;
import static l.files.fs.Files.setLastModifiedTime;
import static l.files.fs.Files.stat;
import static l.files.fs.Files.writeUtf8;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.NOFOLLOW;

@RunWith(AndroidJUnit4.class)
public final class InfoTest extends BaseFilesActivityTest {

    @Test
    public void gets_info_of_multiple_files() throws Exception {

        Path child1 = createFile(dir().resolve("1"));
        Path child2 = createDir(dir().resolve("2"));
        Path child3 = createSymbolicLink(dir().resolve("3"), child2);

        Stat st1 = stat(child1, NOFOLLOW);
        Stat st2 = stat(child2, NOFOLLOW);
        Stat st3 = stat(child3, NOFOLLOW);

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

        Path path = dir().resolve("test.txt");
        writeUtf8(path, "hello world");
        Stat stat = stat(path, NOFOLLOW);

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

        setLastModifiedTime(dir(), NOFOLLOW, EPOCH);
        Path path = createSymbolicLink(dir().resolve("link"), dir());
        Stat stat = stat(path, NOFOLLOW);

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

        Path dir = createDir(dir().resolve("dir"));
        Stat stat = stat(dir, NOFOLLOW);

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

        Path dir = createDir(dir().resolve("dir"));
        Path child1 = createDir(dir.resolve("dir"));
        Path child2 = createFile(dir.resolve("file"));
        Path child3 = createSymbolicLink(dir.resolve("link"), dir);

        Stat stat = stat(dir, NOFOLLOW);
        Stat childStat1 = stat(child1, NOFOLLOW);
        Stat childStat2 = stat(child2, NOFOLLOW);
        Stat childStat3 = stat(child3, NOFOLLOW);

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
