package com.example.files.event;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import java.io.File;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;

public final class MediaDetectedEvent {

  private final File file;
  private final Optional<String> mediaType;

  public MediaDetectedEvent(File file, String mediaType) {
    this.file = checkNotNull(file, "file");
    this.mediaType = fromNullable(mediaType);
  }

  public File file() {
    return file;
  }

  public Optional<String> mediaType() {
    return mediaType;
  }

  @Override public int hashCode() {
    return Objects.hashCode(file(), mediaType());
  }

  @Override public boolean equals(Object o) {
    if (o instanceof MediaDetectedEvent) {
      MediaDetectedEvent that = (MediaDetectedEvent) o;
      return equal(that.file(), file())
          && equal(that.mediaType(), mediaType());
    }
    return false;
  }

  @Override public String toString() {
    return toStringHelper(this)
        .addValue(file())
        .addValue(mediaType())
        .toString();
  }
}
