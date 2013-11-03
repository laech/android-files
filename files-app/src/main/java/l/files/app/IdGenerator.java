package l.files.app;

final class IdGenerator {

  private int current;

  IdGenerator(int seed) {
    current = seed;
  }

  public int get() {
    return (current += 1);
  }
}
