package l.files.operations;

import com.google.auto.value.AutoValue;

import java.io.File;

import l.files.fs.Path;

/**
 * Source and destination of a file task.
 */
@AutoValue
public abstract class Target {

  private static final Target NONE = create("", "");

  Target() {}

  public static Target none() {
    return NONE;
  }

  /**
   * Name of the source file/directory the task is operating from.
   */
  public abstract String source();

  /**
   * Name of the destination file/directory the task is operating to.
   */
  public abstract String destination();

  public static Target create(String source, String destination) {
    return new AutoValue_Target(source, destination);
  }

  /**
   * Creates an instance from the given file paths.
   * {@link #source()} will be the parent's name of the first {@code srcPaths}.
   * {@link #destination()} will be the name of {@code dstPath}
   */
  public static Target fromPaths(Iterable<String> srcPaths, String dstPath) {
    String first = srcPaths.iterator().next();
    String source = new File(first).getParentFile().getName();
    String destination = new File(dstPath).getName();
    return create(source, destination);
  }

  public static Target fromPaths(Iterable<Path> srcPaths, Path dstPath) {
    String source = srcPaths.iterator().next().parent().name();
    String destination = dstPath.name();
    return create(source, destination);
  }

  /**
   * Creates an instance from the given file paths.
   * {@link #source()} and {@link #destination()} will be the parent's name of
   * the first {@code paths}
   */
  public static Target fromPaths(Iterable<String> paths) {
    String first = paths.iterator().next();
    String name = new File(first).getParentFile().getName();
    return create(name, name);
  }

  public static Target fromFsPaths(Iterable<Path> paths) {
    String name = paths.iterator().next().parent().name();
    return create(name, name);
  }
}
