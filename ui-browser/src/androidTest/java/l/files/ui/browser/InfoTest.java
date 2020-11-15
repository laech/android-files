package l.files.ui.browser;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import static android.text.format.DateUtils.*;
import static android.text.format.Formatter.formatFileSize;
import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.time.Instant.EPOCH;
import static java.util.Collections.singleton;

public final class InfoTest extends BaseFilesActivityTest {

    @Test
    public void gets_info_of_multiple_files() throws Exception {

        Path child1 = createFile(dir().resolve("1"));
        Path child2 = createDirectory(dir().resolve("2"));
        Path child3 = createSymbolicLink(dir().resolve("3"), child2);

        BasicFileAttributes attrs1 =
            readAttributes(child1, BasicFileAttributes.class, NOFOLLOW_LINKS);
        BasicFileAttributes attrs2 =
            readAttributes(child2, BasicFileAttributes.class, NOFOLLOW_LINKS);
        BasicFileAttributes attrs3 =
            readAttributes(child3, BasicFileAttributes.class, NOFOLLOW_LINKS);

        screen()
            .longClick(child1)
            .click(child2)
            .click(child3)
            .getInfo()
            .assertSize(formatSizeCount(
                attrs1.size() + attrs2.size() + attrs3.size(), 3));
    }

    @Test
    public void gets_info_of_file() throws Exception {

        Path path = dir().resolve("test.txt");
        write(path, singleton("hello world"));
        BasicFileAttributes attrs =
            readAttributes(path, BasicFileAttributes.class, NOFOLLOW_LINKS);

        screen()
            .longClick(path)
            .getInfo()
            .assertName(path.getFileName().toString())
            .assertDate(formatDate(attrs))
            .assertSize(formatSize(attrs.size()));
    }

    @Test
    public void gets_info_of_link() throws Exception {

        setLastModifiedTime(dir(), FileTime.from(EPOCH));
        Path path = createSymbolicLink(dir().resolve("link"), dir());
        BasicFileAttributes attrs =
            readAttributes(path, BasicFileAttributes.class, NOFOLLOW_LINKS);

        screen()
            .longClick(path)
            .getInfo()
            .assertName(path.getFileName().toString())
            .assertDate(formatDate(attrs))
            .assertSize(formatSize(attrs.size()));
    }

    @Test
    public void gets_info_of_empty_dir() throws Exception {

        Path dir = createDirectory(dir().resolve("dir"));
        BasicFileAttributes attrs =
            readAttributes(dir, BasicFileAttributes.class, NOFOLLOW_LINKS);

        screen()
            .longClick(dir)
            .getInfo()
            .assertName(dir.getFileName().toString())
            .assertDate(formatDate(attrs))
            .assertSize(formatSizeCount(attrs.size(), 1));
    }

    @Test
    public void gets_info_of_non_empty_dir() throws Exception {

        Path dir = createDirectory(dir().resolve("dir"));
        Path child1 = createDirectory(dir.resolve("dir"));
        Path child2 = createFile(dir.resolve("file"));
        Path child3 = createSymbolicLink(dir.resolve("link"), dir);

        BasicFileAttributes stat =
            readAttributes(dir, BasicFileAttributes.class, NOFOLLOW_LINKS);
        BasicFileAttributes childStat1 =
            readAttributes(child1, BasicFileAttributes.class, NOFOLLOW_LINKS);
        BasicFileAttributes childStat2 =
            readAttributes(child2, BasicFileAttributes.class, NOFOLLOW_LINKS);
        BasicFileAttributes childStat3 =
            readAttributes(child3, BasicFileAttributes.class, NOFOLLOW_LINKS);

        screen()
            .longClick(dir)
            .getInfo()
            .assertName(dir.getFileName().toString())
            .assertDate(formatDate(stat))
            .assertSize(formatSizeCount(
                stat.size()
                    + childStat1.size()
                    + childStat2.size()
                    + childStat3.size(),
                4
            ));
    }

    private String formatDate(BasicFileAttributes attrs) {
        long millis = attrs.lastModifiedTime().toMillis();
        int flags = FORMAT_SHOW_DATE | FORMAT_SHOW_TIME;
        return formatDateTime(getActivity(), millis, flags);
    }


    private String formatSize(long size) {
        return formatFileSize(getActivity(), size);
    }

    private String formatSizeCount(long size, int count) {
        return getActivity().getResources().getQuantityString(
            l.files.ui.info.R.plurals.x_size_y_items,
            count,
            formatSize(size),
            count
        );
    }

}
