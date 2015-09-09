package l.files.operations;

import com.google.auto.value.AutoValue;

import l.files.fs.File;
import l.files.fs.File.Name;

/**
 * Source and destination of a file task.
 */
@AutoValue
public abstract class Target {

  public static final Target NONE = create("", "");

  Target() {
  }

  /**
   * Name of the source file/directory the task is operating from.
   */
  public abstract Name source();

  /**
   * Name of the destination file/directory the task is operating to.
   */
  public abstract Name destination();

  public static Target create(String source, String destination) {
    return create(Name.of(source), Name.of(destination));
  }

  public static Target create(Name source, Name destination) {
    return new AutoValue_Target(source, destination);
  }

  public static Target from(Iterable<? extends File> sources, File destination) {
    File src = sources.iterator().next().parent();
    assert src != null;
    return create(src.name(), destination.name());
  }

  public static Target from(Iterable<? extends File> resources) {
    File parent = resources.iterator().next().parent();
    assert parent != null;
    return create(parent.name(), parent.name());
  }

}
