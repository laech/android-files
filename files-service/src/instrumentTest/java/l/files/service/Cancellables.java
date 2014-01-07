package l.files.service;

public final class Cancellables {

  static final Cancellable CANCELLED = new Cancellable() {
    @Override public boolean isCancelled() {
      return true;
    }
  };

  static final Cancellable NO_CANCEL = new Cancellable() {
    @Override public boolean isCancelled() {
      return false;
    }
  };

  private Cancellables() {}
}
