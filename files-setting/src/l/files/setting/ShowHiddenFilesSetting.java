package l.files.setting;

import l.files.common.base.Value;

/**
 * This event will be fired when the show/hide hidden files preference is
 * updated, and it will also be fired on initial registration to the event bus
 * for providing the current preference.
 */
public final class ShowHiddenFilesSetting extends Value<Boolean> {

  public ShowHiddenFilesSetting(boolean show) {
    super(show);
  }
}
