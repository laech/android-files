package l.files.fs;

import com.google.auto.value.AutoValue;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Locale.ENGLISH;

/**
 * Represents a URI scheme.
 *
 * @see <a href="http://tools.ietf.org/html/rfc3986#section-3.1">URI Scheme</a>
 */
@AutoValue
public abstract class Scheme {
  private static final Pattern PATTERN =
      Pattern.compile("\\p{Alpha}[-+.\\p{Alnum}]*");

  Scheme() {}

  abstract String value();

  /**
   * Creates an instance from the given scheme
   *
   * @throws NullPointerException     if scheme is null
   * @throws IllegalArgumentException if the given scheme is invalid
   */
  public static Scheme parse(String scheme) {
    checkNotNull(scheme, "scheme");
    if (!PATTERN.matcher(scheme).matches()) {
      throw new IllegalArgumentException(scheme);
    }
    return new AutoValue_Scheme(scheme.toLowerCase(ENGLISH));
  }

  @Override public int hashCode() {
    return value().hashCode();
  }

  @Override public String toString() {
    return value();
  }
}
