package l.files.common.testing.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.File;

public final class FileMatchers {
  private FileMatchers() {}

  public static Matcher<File> exists() {
    return new TypeSafeMatcher<File>() {
      @Override protected boolean matchesSafely(File file) {
        return file.exists();
      }

      @Override public void describeTo(Description description) {
        description.appendText("file to exist");
      }

      @Override
      protected void describeMismatchSafely(File file, Description description) {
        description
            .appendText("file does not exists: ")
            .appendText(file.getPath());
      }
    };
  }
}
