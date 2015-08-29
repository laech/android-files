package l.files.ui.browser;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.text.Collator;

import auto.parcel.AutoParcel;
import collation.NaturalKey;
import l.files.fs.BasicDetector;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static l.files.fs.Detector.OCTET_STREAM;

public abstract class FileListItem {

  FileListItem() {
  }

  public abstract boolean isFile();

  public boolean isHeader() {
    return !isFile();
  }

  @AutoParcel
  public static abstract class Header extends FileListItem {

    Header() {
    }

    public abstract String header();

    public static Header of(String header) {
      return new AutoParcel_FileListItem_Header(header);
    }

    @Override public boolean isFile() {
      return false;
    }

    @Override public String toString() {
      return header();
    }
  }

  @AutoParcel
  public static abstract class File extends FileListItem implements Comparable<File> {

    private Boolean readable;

    File() {
    }

    // TODO don't do the following in the main thread

    public boolean isReadable() {
      if (readable == null) {
        try {
          readable = resource().readable();
        } catch (IOException e) {
          readable = false;
        }
      }
      return readable;
    }

    public String basicMediaType() {
      try {
        return BasicDetector.INSTANCE.detect(resource());
      } catch (IOException e) {
        return OCTET_STREAM;
      }
    }

    public abstract Resource resource();

    @Nullable public abstract Stat stat(); // TODO

    @Nullable abstract Stat _targetStat();

    abstract NaturalKey collationKey();

    public static File create(
        Resource resource,
        @Nullable Stat stat,
        @Nullable Stat targetStat,
        Collator collator) {
      String name = resource.name().toString();
      NaturalKey key = NaturalKey.create(collator, name);
      return new AutoParcel_FileListItem_File(resource, stat, targetStat, key);
    }

    @Override public boolean isFile() {
      return true;
    }

    /**
     * If the resource is a link, this returns the status of the target
     * file, if not available, returns the status of the link.
     */
    @Nullable public Stat targetStat() {
      return _targetStat() != null ? _targetStat() : stat();
    }

    @Override public int compareTo(File another) {
      return collationKey().compareTo(another.collationKey());
    }
  }

}
