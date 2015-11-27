package l.files.ui.base.selection;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.view.ActionMode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.AbstractMap.SimpleEntry;

import l.files.ui.base.BuildConfig;
import l.files.ui.base.view.ActionModeProvider;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = JELLY_BEAN)
public final class SelectionModeFragmentTest {

    @Test
    public void test_maintains_action_mode_on_recreation() throws Exception {
        Bundle state = new Bundle();
        SelectionModeFragmentTester f1 = new SelectionModeFragmentTester();
        f1.onCreate(null);
        f1.selection().add(1, "one");
        f1.onSaveInstanceState(state);
        verifyZeroInteractions(f1.actionModeProvider());

        SelectionModeFragmentTester f2 = new SelectionModeFragmentTester();
        f2.onCreate(state);
        f2.onViewStateRestored(state);
        assertEquals(
                singleton(new SimpleEntry<>(1, "one")),
                f2.selection().copy().entrySet());
        verify(f2.actionModeProvider()).startSupportActionMode(f2.actionModeCallback());
    }

    @SuppressLint("ValidFragment")
    private static final class SelectionModeFragmentTester
            extends SelectionModeFragment<Integer, Object> {

        private final ActionModeProvider provider = mock(ActionModeProvider.class);
        private final ActionMode.Callback callback = mock(ActionMode.Callback.class);

        @Override
        protected ActionMode.Callback actionModeCallback() {
            return callback;
        }

        @Override
        protected ActionModeProvider actionModeProvider() {
            return provider;
        }
    }

}
