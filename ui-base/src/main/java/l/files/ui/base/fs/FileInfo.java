package l.files.ui.base.fs;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import l.files.base.text.CollationKey;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Collator;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class FileInfo implements Comparable<FileInfo> {

    private final Collator collator;

    @Nullable
    private CollationKey collationKey;

    @Nullable
    private Boolean readable;

    private final Path selfPath;

    @Nullable
    private final BasicFileAttributes selfAttrs;

    @Nullable
    private final Path linkTargetPath;

    @Nullable
    private final BasicFileAttributes linkTargetAttrs;

    private FileInfo(
        Path selfPath,
        @Nullable BasicFileAttributes selfAttrs,
        @Nullable Path linkTargetPath,
        @Nullable BasicFileAttributes linkTargetAttrs,
        Collator collator
    ) {

        this.selfPath = requireNonNull(selfPath);
        this.selfAttrs = selfAttrs;
        this.linkTargetPath = linkTargetPath;
        this.linkTargetAttrs = linkTargetAttrs;
        this.collator = requireNonNull(collator);
    }

    @DrawableRes
    public int iconDrawableResourceId() {
        if (selfAttrs != null && selfAttrs.isDirectory()) {
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
    public BasicFileAttributes selfAttrs() {
        return selfAttrs;
    }

    @Nullable
    public Path linkTargetPath() {
        return linkTargetPath;
    }

    @Nullable
    private BasicFileAttributes linkTargetAttrs() {
        return linkTargetAttrs;
    }

    @Nullable
    public BasicFileAttributes linkTargetOrSelfAttrs() {
        return linkTargetAttrs() != null ? linkTargetAttrs() : selfAttrs();
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
            ", selfAttrs=" + selfAttrs +
            ", linkTargetPath=" + linkTargetPath +
            ", linkTargetAttrs=" + linkTargetAttrs +
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

        return Objects.equals(selfPath, that.selfPath)
            && Objects.equals(selfAttrs, that.selfAttrs)
            && Objects.equals(linkTargetPath, that.linkTargetPath)
            && Objects.equals(linkTargetAttrs, that.linkTargetAttrs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            selfPath,
            selfAttrs,
            linkTargetPath,
            linkTargetAttrs
        );
    }

    public static FileInfo create(
        Path path,
        @Nullable BasicFileAttributes attrs,
        @Nullable Path target,
        @Nullable BasicFileAttributes targetAttrs,
        Collator collator
    ) {
        return new FileInfo(path, attrs, target, targetAttrs, collator);
    }

}
