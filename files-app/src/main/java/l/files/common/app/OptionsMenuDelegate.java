package l.files.common.app;

import android.view.Menu;
import android.view.MenuItem;

import static java.util.Objects.requireNonNull;

public class OptionsMenuDelegate implements OptionsMenu {

    private final OptionsMenu delegate;

    public OptionsMenuDelegate(OptionsMenu delegate) {
        this.delegate = requireNonNull(delegate, "delegate");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        delegate.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        delegate.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return delegate.onOptionsItemSelected(item);
    }
}
