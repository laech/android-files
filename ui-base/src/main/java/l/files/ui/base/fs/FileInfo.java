package l.files.ui.base.fs;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

import java.io.IOException;

import l.files.base.Provider;
import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.media.MediaTypes;

import static l.files.fs.media.MediaTypes.MEDIA_TYPE_OCTET_STREAM;

@AutoValue
public abstract class FileInfo implements Comparable<FileInfo> {

    private Provider<Collator> collator;
    private CollationKey collationKey;
    private Boolean readable;
    private String basicMediaType;

    FileInfo() {
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

    public abstract Path selfPath();

    @Nullable
    public abstract Stat selfStat();

    @Nullable
    public abstract Path linkTargetPath();

    @Nullable
    public abstract Stat linkTargetStat();

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

    public static FileInfo create(
            Path path,
            @Nullable Stat stat,
            @Nullable Path target,
            @Nullable Stat targetStat,
            Provider<Collator> collator) {
        FileInfo item = new AutoValue_FileInfo(path, stat, target, targetStat);
        item.collator = collator;
        return item;
    }
}
