package l.files.event;

import l.files.common.base.Value;

/**
 * Event representing a request to show/hide hidden files.
 */
public final class ShowHiddenFilesRequest extends Value<Boolean> {

  public ShowHiddenFilesRequest(boolean show) {
    super(show);
  }
}
