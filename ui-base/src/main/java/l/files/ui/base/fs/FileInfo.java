package l.files.ui.base.fs;

import android.support.annotation.DrawableRes;

import java.io.IOException;
import java.text.Collator;

import javax.annotation.Nullable;

import l.files.base.Objects;
import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.text.CollationKey;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.R.drawable.ic_file_download_black_24dp;
import static l.files.ui.base.R.drawable.ic_folder_black_24dp;
import static l.files.ui.base.R.drawable.ic_home_black_24dp;
import static l.files.ui.base.R.drawable.ic_insert_drive_file_black_24dp;
import static l.files.ui.base.R.drawable.ic_library_music_black_24dp;
import static l.files.ui.base.R.drawable.ic_phone_android_black_24dp;
import static l.files.ui.base.R.drawable.ic_photo_library_black_24dp;
import static l.files.ui.base.R.drawable.ic_sd_storage_black_24dp;
import static l.files.ui.base.R.drawable.ic_video_library_black_24dp;
import static l.files.ui.base.fs.UserDirs.DIR_DCIM;
import static l.files.ui.base.fs.UserDirs.DIR_DOWNLOADS;
import static l.files.ui.base.fs.UserDirs.DIR_HOME;
import static l.files.ui.base.fs.UserDirs.DIR_MOVIES;
import static l.files.ui.base.fs.UserDirs.DIR_MUSIC;
import static l.files.ui.base.fs.UserDirs.DIR_PICTURES;
import static l.files.ui.base.fs.UserDirs.DIR_ROOT;
import static l.files.ui.base.fs.UserDirs.DIR_SDCARD2;

public final class FileInfo implements Comparable<FileInfo> {

    private final Collator collator;

    @Nullable
    private CollationKey collationKey;

    @Nullable
    private Boolean readable;

    private final Path selfPath;

    @Nullable
    private final Stat selfStat;

    @Nullable
    private final Path linkTargetPath;

    @Nullable
    private final Stat linkTargetStat;

    private FileInfo(
            Path selfPath,
            @Nullable Stat selfStat,
            @Nullable Path linkTargetPath,
            @Nullable Stat linkTargetStat,
            Collator collator) {

        this.selfPath = requireNonNull(selfPath);
        this.selfStat = selfStat;
        this.linkTargetPath = linkTargetPath;
        this.linkTargetStat = linkTargetStat;
        this.collator = requireNonNull(collator);
    }

    @DrawableRes
    public int iconDrawableResourceId() {
        Path p = selfPath();
        if (p.equals(DIR_ROOT)) return ic_phone_android_black_24dp;
        if (p.equals(DIR_HOME)) return ic_home_black_24dp;
        if (p.equals(DIR_DCIM)) return ic_photo_library_black_24dp;
        if (p.equals(DIR_MUSIC)) return ic_library_music_black_24dp;
        if (p.equals(DIR_MOVIES)) return ic_video_library_black_24dp;
        if (p.equals(DIR_PICTURES)) return ic_photo_library_black_24dp;
        if (p.equals(DIR_DOWNLOADS)) return ic_file_download_black_24dp;
        if (p.equals(DIR_SDCARD2)) return ic_sd_storage_black_24dp;
        if (selfStat != null && selfStat.isDirectory()) return ic_folder_black_24dp;
        return ic_insert_drive_file_black_24dp;
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

    public String name() {
        return selfPath().name().toString();
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
        Path path = linkTargetPath();
        return path != null ? path : selfPath();
    }

    private CollationKey collationKey() {
        if (collationKey == null) {
            collationKey = CollationKey.create(
                    collator, selfPath().name().toString());
        }
        return collationKey;
    }

    @Override
    public int compareTo(FileInfo that) {
        return collationKey().compareTo(that.collationKey());
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
    public boolean equals(@Nullable Object o) {
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
            Collator collator) {
        return new FileInfo(path, stat, target, targetStat, collator);
    }

}
