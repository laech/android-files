package l.files.provider;

import android.webkit.MimeTypeMap;

import com.google.common.base.Predicate;

import java.io.File;
import java.io.IOException;

import l.files.io.file.FileInfo;
import l.files.io.file.Path;

import static com.google.common.base.Predicates.not;
import static java.util.Locale.ENGLISH;
import static l.files.common.database.DataTypes.booleanToInt;
import static l.files.common.database.DataTypes.intToBoolean;
import static l.files.provider.FilesContract.FileInfo.MIME_DIR;
import static l.files.provider.FilesContract.getFileLocation;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

final class FileData {

    static final Predicate<FileData> HIDDEN = new Predicate<FileData>() {
        @Override
        public boolean apply(FileData input) {
            return intToBoolean(input.hidden);
        }
    };

    static final Predicate<FileData> NOT_HIDDEN = not(HIDDEN);

    final long lastModified;
    final long length;
    final int directory;
    final int hidden;
    final int canRead;
    final int canWrite;
    final String name;
    final String path;
    final String location;
    final String mime;

    private FileData(FileInfo info) {
        File file = info.toFile();
        this.lastModified = info.getLastModified();
        this.length = info.getSize();
        this.directory = booleanToInt(info.isDirectory());
        this.canRead = booleanToInt(file.canRead());
        this.canWrite = booleanToInt(file.canWrite());
        this.name = file.getName();
        this.path = info.getPath();
        this.location = getFileLocation(file);
        this.mime = mime(name, info.isDirectory());
        this.hidden = booleanToInt(name.startsWith("."));
    }

    public static FileData get(Path path) throws IOException {
        return new FileData(FileInfo.get(path.toString()));
    }

    private static String mime(String name, boolean isDirectory) {
        if (isDirectory) {
            return MIME_DIR;
        }
        String ext = getExtension(name).toLowerCase(ENGLISH);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return mime == null ? "application/octet-stream" : mime;
    }

    @Override
    public String toString() {
        return reflectionToString(this, SHORT_PREFIX_STYLE);
    }
}
