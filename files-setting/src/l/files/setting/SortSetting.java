package l.files.setting;

import l.files.common.base.Value;

/**
 * This event will be fired when the sorting preference is updated, and it will
 * also be fired on initial registration to the event bus for providing the
 * current preference.
 */
public final class SortSetting extends Value<String> {

  public SortSetting(String sort) {
    super(sort);
  }
}
