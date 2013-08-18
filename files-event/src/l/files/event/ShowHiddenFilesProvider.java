package l.files.event;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

final class ShowHiddenFilesProvider extends SettingProvider<ShowHiddenFilesSetting> {

  private boolean showByDefault;

  ShowHiddenFilesProvider(boolean showByDefault) {
    super("show-hidden-files");
    this.showByDefault = showByDefault;
  }

  @Produce @Override public ShowHiddenFilesSetting get() {
    return new ShowHiddenFilesSetting(pref.getBoolean(key, showByDefault));
  }

  @Subscribe public void set(ShowHiddenFilesRequest request) {
    pref.edit()
        .putBoolean(key, request.value())
        .apply();
  }
}
