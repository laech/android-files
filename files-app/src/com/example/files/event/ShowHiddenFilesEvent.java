package com.example.files.event;

import static com.google.common.base.Objects.toStringHelper;

public final class ShowHiddenFilesEvent {

  private final boolean show;

  public ShowHiddenFilesEvent(boolean show) {
    this.show = show;
  }

  public boolean show() {
    return show;
  }

  @Override public boolean equals(Object o) {
    if (o instanceof ShowHiddenFilesEvent) {
      ShowHiddenFilesEvent that = (ShowHiddenFilesEvent) o;
      return that.show() == show();
    }
    return false;
  }

  @Override public int hashCode() {
    return show() ? 1 : 0;
  }

  @Override public String toString() {
    return toStringHelper(this).addValue(show()).toString();
  }
}
