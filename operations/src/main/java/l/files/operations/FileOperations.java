package l.files.operations;

final class FileOperations {
  private FileOperations() {}

  static void checkInterrupt() throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
  }
}
