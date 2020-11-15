package l.files.ui.base.fs;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import l.files.base.Objects;
import l.files.base.text.CollationKey;
import l.files.fs.Stat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.util.Optional;

import static l.files.base.Objects.requireNonNull;

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
        Collator collator
    ) {

        this.selfPath = requireNonNull(selfPath);
        this.selfStat = selfStat;
        this.linkTargetPath = linkTargetPath;
        this.linkTargetStat = linkTargetStat;
        this.collator = requireNonNull(collator);
    }

    @DrawableRes
    public int iconDrawableResourceId() {
        if (selfStat != null && selfStat.isDirectory()) {
            return FileIcons.getDirectory(selfPath());
        }
        return FileIcons.getFile();
    }

    public boolean isReadable() {
        if (readable == null) {
            readable = Files.isReadable(selfPath());
        }
        return readable;
    }

    public String name() {
        return Optional.ofNullable(selfPath().getFileName())
            .map(Path::toString)
            .orElse("");
    }

    public Path getFileName() {
        return selfPath().getFileName();
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
    private Stat linkTargetStat() {
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
            collationKey = CollationKey.create(collator, name());
        }
        return collationKey;
    }

    @Override
    public int compareTo(@NonNull FileInfo that) {
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

        return Objects.equal(selfPath, that.selfPath)
            && Objects.equal(selfStat, that.selfStat)
            && Objects.equal(linkTargetPath, that.linkTargetPath)
            && Objects.equal(linkTargetStat, that.linkTargetStat);
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
        Collator collator
    ) {
        return new FileInfo(path, stat, target, targetStat, collator);
    }

}
