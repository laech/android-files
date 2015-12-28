package l.files.ui.browser;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.net.URI;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.selection.Selection;

import static android.content.Intent.ACTION_CHOOSER;
import static android.content.Intent.ACTION_SEND_MULTIPLE;
import static android.content.Intent.EXTRA_INTENT;
import static android.content.Intent.EXTRA_STREAM;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static java.util.Arrays.asList;
import static l.files.fs.Files.MEDIA_TYPE_OCTET_STREAM;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = JELLY_BEAN)
public final class ShareTest {

    private Selection<Object, FileInfo> selection;
    private Share action;

    @Before
    public void setUp() throws Exception {
        selection = new Selection<>();
        action = new Share(selection, application);
    }

    @Test
    public void on_click_finishes_action_mode() throws Exception {
        ActionMode mode = mock(ActionMode.class);
        MenuItem item = mock(MenuItem.class);
        action.onItemSelected(mode, item);
        verify(mode).finish();
    }

    @Test
    public void disables_action_if_selection_contains_non_file() throws Exception {

        MenuItem item = mock(MenuItem.class);
        Menu menu = mock(Menu.class);
        given(menu.findItem(R.id.share)).willReturn(item);

        MenuInflater inflater = mock(MenuInflater.class);
        ActionMode mode = mock(ActionMode.class);
        given(mode.getMenuInflater()).willReturn(inflater);
        given(mode.getMenu()).willReturn(menu);

        InOrder order = inOrder(item);

        action.onCreateActionMode(mode, menu);
        action.onPrepareActionMode(mode, menu);
        order.verify(item).setEnabled(false);
        order.verifyNoMoreInteractions();

        selection.add(1, mockFileItem("file:/tmp/1", true));
        order.verify(item).setEnabled(true);
        order.verifyNoMoreInteractions();

        selection.add(2, mockFileItem("file:/tmp/2", false));
        order.verify(item).setEnabled(false);
        order.verifyNoMoreInteractions();
    }

    @Test
    public void on_click_starts_share_intent() throws Exception {
        selection.add(1, mockFileItem("file:/tmp/1"));
        selection.add(2, mockFileItem("file:/tmp/2"));

        action.onItemSelected(mock(ActionMode.class), mock(MenuItem.class));

        Intent chooserIntent = shadowOf(application).getNextStartedActivity();
        assertEquals(ACTION_CHOOSER, chooserIntent.getAction());

        Intent shareIntent = chooserIntent.getParcelableExtra(EXTRA_INTENT);
        assertEquals(ACTION_SEND_MULTIPLE, shareIntent.getAction());
        assertEquals(MEDIA_TYPE_OCTET_STREAM, shareIntent.getType());
        assertEquals(
                asList(Uri.parse("file:/tmp/1"), Uri.parse("file:/tmp/2")),
                shareIntent.getParcelableArrayListExtra(EXTRA_STREAM));
    }

    private FileInfo mockFileItem(String uri) {
        return mockFileItem(uri, true);
    }

    private FileInfo mockFileItem(String uri, boolean isRegularFile) {
        Path file = mock(Path.class);
        Stat stat = mock(Stat.class);
        given(stat.isRegularFile()).willReturn(isRegularFile);
        given(file.toUri()).willReturn(URI.create(uri));
        return FileInfo.create(file, stat, null, null, null);
    }
}
