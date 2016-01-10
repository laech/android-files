package l.files.ui.base.fs;

import android.support.annotation.Nullable;

import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

import java.io.IOException;

import l.files.base.Objects;
import l.files.base.Provider;
import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.media.MediaTypes;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.media.MediaTypes.MEDIA_TYPE_OCTET_STREAM;

public final class FileInfo implements Comparable<FileInfo> {

    private Provider<Collator> collator;
    private CollationKey collationKey;
    private Boolean readable;
    private String basicMediaType;

    private final Path selfPath;
    private final Stat selfStat;
    private final Path linkTargetPath;
    private final Stat linkTargetStat;

    private FileInfo(
            Path selfPath,
            @Nullable Stat selfStat,
            @Nullable Path linkTargetPath,
            @Nullable Stat linkTargetStat) {

        this.selfPath = requireNonNull(selfPath);
        this.selfStat = selfStat;
        this.linkTargetPath = linkTargetPath;
        this.linkTargetStat = linkTargetStat;
    }

    public boolean isReadable() {
        if (readable == null) {
            try {
                readable = Files.isReadable(selfPath());
            } catch (IOException e) {
                readable = false;
            }
        }
        return readable;
    }

    public String basicMediaType() {
        if (basicMediaType == null) {
            try {
                basicMediaType = MediaTypes.detectByProperties(
                        selfPath(), linkTargetOrSelfStat());
            } catch (IOException e) {
                basicMediaType = MEDIA_TYPE_OCTET_STREAM;
            }
        }
        return basicMediaType;
    }

    public Path selfPath() {
        return selfPath;
    }

    @Nullable
    public Stat selfStat() {
        return selfStat;
    }

    @Nullable
    public Path linkTargetPath() {
        return linkTargetPath;
    }

    @Nullable
    public Stat linkTargetStat() {
        return linkTargetStat;
    }

    @Nullable
    public Stat linkTargetOrSelfStat() {
        return linkTargetStat() != null ? linkTargetStat() : selfStat();
    }

    public Path linkTargetOrSelfPath() {
        return linkTargetPath() != null ? linkTargetPath() : selfPath();
    }

    private CollationKey collationKey() {
        if (collationKey == null) {
            collationKey = collator.get()
                    .getCollationKey(selfPath().name().toString());
        }
        return collationKey;
    }

    @Override
    public int compareTo(FileInfo another) {
        return collationKey().compareTo(another.collationKey());
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "selfPath=" + selfPath +
                ", selfStat=" + selfStat +
                ", linkTargetPath=" + linkTargetPath +
                ", linkTargetStat=" + linkTargetStat +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FileInfo that = (FileInfo) o;

        return Objects.equal(selfPath, that.selfPath) &&
                Objects.equal(selfStat, that.selfStat) &&
                Objects.equal(linkTargetPath, that.linkTargetPath) &&
                Objects.equal(linkTargetStat, that.linkTargetStat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selfPath, selfStat, linkTargetPath, linkTargetStat);
    }

    public static FileInfo create(
            Path path,
            @Nullable Stat stat,
            @Nullable Path target,
            @Nullable Stat targetStat,
            Provider<Collator> collator) {
        FileInfo item = new FileInfo(path, stat, target, targetStat);
        item.collator = collator;
        return item;
    }
}
