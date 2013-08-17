package l.files.setting;

import l.files.common.base.Value;

/**
 * Event representing a request for changing the way files are sorted.
 */
public final class SortRequest extends Value<String> {

  public SortRequest(String sort) {
    super(sort);
  }
}
