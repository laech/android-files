package l.files.test;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.View;

import static android.view.MenuItem.OnActionExpandListener;
import static android.view.MenuItem.OnMenuItemClickListener;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyChar;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;

public final class Mocks {

    public static MenuItem mockMenuItem() {
        return mockMenuItem(0);
    }

    public static MenuItem mockMenuItem(int id) {
        MenuItem item = mock(MenuItem.class);
        given(item.getItemId()).willReturn(id);
        given(item.setActionProvider(any(ActionProvider.class))).willReturn(item);
        given(item.setActionView(anyInt())).willReturn(item);
        given(item.setActionView(any(View.class))).willReturn(item);
        given(item.setAlphabeticShortcut(anyChar())).willReturn(item);
        given(item.setCheckable(anyBoolean())).willReturn(item);
        given(item.setChecked(anyBoolean())).willReturn(item);
        given(item.setEnabled(anyBoolean())).willReturn(item);
        given(item.setIcon(any(Drawable.class))).willReturn(item);
        given(item.setIcon(anyInt())).willReturn(item);
        given(item.setIntent(any(Intent.class))).willReturn(item);
        given(item.setNumericShortcut(anyChar())).willReturn(item);
        given(item.setOnActionExpandListener(any(OnActionExpandListener.class))).willReturn(item);
        given(item.setOnMenuItemClickListener(any(OnMenuItemClickListener.class))).willReturn(item);
        given(item.setShortcut(anyChar(), anyChar())).willReturn(item);
        given(item.setShowAsActionFlags(anyInt())).willReturn(item);
        given(item.setTitle(any(CharSequence.class))).willReturn(item);
        given(item.setTitle(anyInt())).willReturn(item);
        given(item.setTitleCondensed(any(CharSequence.class))).willReturn(item);
        given(item.setVisible(anyBoolean())).willReturn(item);
        return item;
    }

    private Mocks() {
    }
}
