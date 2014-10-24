package l.files.common.app;

import android.view.Menu;
import android.view.MenuItem;

import static com.google.common.base.Preconditions.checkNotNull;

public class OptionsMenuDelegate implements OptionsMenu {

  private final OptionsMenu delegate;

  public OptionsMenuDelegate(OptionsMenu delegate) {
    this.delegate = checkNotNull(delegate, "delegate");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    delegate.onCreateOptionsMenu(menu);
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    delegate.onPrepareOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    return delegate.onOptionsItemSelected(item);
  }
}
