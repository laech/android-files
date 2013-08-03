package l.files.setting;

/**
 * Event representing a request for changing the way files are sorted.
 */
public final class SortRequest extends Value<String> {

  public SortRequest(String sort) {
    super(sort);
  }
}
