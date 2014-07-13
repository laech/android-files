package l.files.io.file.event;

public class WatchException extends RuntimeException {

  public WatchException(String msg, Throwable e) {
    super(msg, e);
  }
}
