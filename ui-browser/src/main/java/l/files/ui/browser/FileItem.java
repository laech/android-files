package l.files.ui.browser;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

import java.io.IOException;

import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Stat;

import static l.files.fs.Files.MEDIA_TYPE_OCTET_STREAM;

@AutoValue
abstract class FileItem implements BrowserItem, Comparable<FileItem> {

    private Provider<Collator> collator;
    private CollationKey collationKey;
    private Boolean readable;
    private String basicMediaType;

    FileItem() {
    }

    boolean isReadable() {
        if (readable == null) {
            try {
                readable = Files.isReadable(selfPath());
            } catch (IOException e) {
                readable = false;
            }
        }
        return readable;
    }

    String basicMediaType() {
        if (basicMediaType == null) {
            try {
                basicMediaType = Files.detectBasicMediaType(
                        selfPath(), linkTargetOrSelfStat());
            } catch (IOException e) {
                basicMediaType = MEDIA_TYPE_OCTET_STREAM;
            }
        }
        return basicMediaType;
    }

    abstract Path selfPath();

    @Nullable
    abstract Stat selfStat();

    @Nullable
    abstract Path linkTargetPath();

    @Nullable
    abstract Stat linkTargetStat();

    @Nullable
    Stat linkTargetOrSelfStat() {
        return linkTargetStat() != null ? linkTargetStat() : selfStat();
    }

    Path linkTargetOrSelfPath() {
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
    public int compareTo(FileItem another) {
        return collationKey().compareTo(another.collationKey());
    }

    static FileItem create(
            Path path,
            @Nullable Stat stat,
            @Nullable Path target,
            @Nullable Stat targetStat,
            Provider<Collator> collator) {
        FileItem item = new AutoValue_FileItem(path, stat, target, targetStat);
        item.collator = collator;
        return item;
    }
}
