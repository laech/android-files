package l.files.ui.operations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import static l.files.ui.base.view.Views.find;

public final class FailuresActivity extends AppCompatActivity {

    private static final String EXTRA_FAILURES = "failures";
    private static final String EXTRA_TITLE = "title";

    public static String getTitle(Intent intent) {
        return intent.getStringExtra(EXTRA_TITLE);
    }

    public static Collection<FailureMessage> getFailures(Intent intent) {
        return intent.getParcelableArrayListExtra(EXTRA_FAILURES);
    }

    public static Intent newIntent(Context context, String title, Collection<FailureMessage> failures) {
        return new Intent(context, FailuresActivity.class)
                .putExtra(EXTRA_TITLE, title)
                .putParcelableArrayListExtra(EXTRA_FAILURES, new ArrayList<>(failures));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.failures_activity);
        setSupportActionBar(find(R.id.toolbar, this));
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getIntent().getStringExtra(EXTRA_TITLE));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        List<FailureMessage> failures = getIntent().getParcelableArrayListExtra(EXTRA_FAILURES);
        getFailuresFragment().setFailures(failures);
    }

    private FailuresFragment getFailuresFragment() {
        return (FailuresFragment) getFragmentManager().findFragmentById(R.id.failures_fragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
