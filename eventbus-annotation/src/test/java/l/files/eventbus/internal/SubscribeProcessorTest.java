package l.files.eventbus.internal;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;

import javax.tools.JavaFileObject;

import de.greenrobot.event.ThreadMode;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.junit.runners.Parameterized.Parameters;
import static org.truth0.Truth.ASSERT;

@RunWith(Parameterized.class)
public final class SubscribeProcessorTest {

  @Parameters(name = "{0}") public static Collection<Object[]> data() {
    Collection<Object[]> data = new ArrayList<>();
    for (ThreadMode mode : ThreadMode.values()) {
      data.add(new Object[]{mode});
    }
    return data;
  }

  private static final String TEMPLATE =
      "import l.files.eventbus.Subscribe;\n" +
          "import de.greenrobot.event.ThreadMode;\n" +
          "class Hello {\n" +
          "  @Subscribe(ThreadMode.{mode})\n" +
          "  public void onEvent{suffix}({params}) {}\n" +
          "}";

  /**
   * The line number of the event method declaration (i.e. public void onEvent)
   * in {@link #TEMPLATE}.
   */
  private static final int EVENT_METHOD_LINE_NUMBER = 5;

  /**
   * The column at which the event method name starts in {@link #TEMPLATE}.
   * <pre>
   * public void onEvent
   *             ^
   * </pre>
   */
  private static final int EVENT_METHOD_NAME_COLUMN = 15;

  private JavaFileObject source(Object mode, Object suffix, Object params) {
    return JavaFileObjects.forSourceString("Hello", TEMPLATE
        .replace("{mode}", mode.toString())
        .replace("{suffix}", suffix.toString())
        .replace("{params}", params.toString()));
  }

  /**
   * True if method name should be suffixed with thread mode.
   */
  private final boolean suffix;
  private final ThreadMode mode;

  public SubscribeProcessorTest(ThreadMode mode) {
    this.mode = mode;
    this.suffix = !mode.equals(ThreadMode.PostThread);
  }

  private String getEventMethodSuffix() {
    return suffix ? mode.name() : "";
  }

  @Test
  public void compilesSuccessfullyOnEventMethodNameSuffixedWithThreadMode() {
    ASSERT.about(javaSource())
        .that(source(mode, getEventMethodSuffix(), "Object o"))
        .processedWith(new SubscribeProcessor())
        .compilesWithoutError();
  }

  @Test
  public void omitsCompilerErrorOnWrongMethodName() {
    omitsCompilerErrorOnEventMethod(
        "hello", "Object o", "\"onEvent" + getEventMethodSuffix() + "\"");
  }

  @Test
  public void omitsCompilerErrorOnNoParameter() {
    omitsCompilerErrorOnWrongNumberOfParameters("");
  }

  @Test
  public void omitsCompilerErrorOnMoreThanOneParameter() {
    omitsCompilerErrorOnWrongNumberOfParameters("Object a, Object b");
  }

  private void omitsCompilerErrorOnWrongNumberOfParameters(String params) {
    String suffix = getEventMethodSuffix();
    omitsCompilerErrorOnEventMethod(suffix, params, "one argument");
  }

  /**
   * Test a compiler error is omitted on the event method at the correct
   * position.
   *
   * @param suffix the suffix to append to the method name
   * @param params the method parameters (e.g. "Object a, Object b")
   * @param msg    the message segment expected to appear in the error message
   * @see #EVENT_METHOD_LINE_NUMBER
   * @see #EVENT_METHOD_NAME_COLUMN
   */
  private void omitsCompilerErrorOnEventMethod(
      String suffix, String params, String msg) {
    JavaFileObject source = source(mode, suffix, params);
    ASSERT.about(javaSource())
        .that(source)
        .processedWith(new SubscribeProcessor())
        .failsToCompile()
        .withErrorContaining(msg)
        .in(source)
        .onLine(EVENT_METHOD_LINE_NUMBER)
        .atColumn(EVENT_METHOD_NAME_COLUMN);
  }

}
