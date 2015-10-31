package l.files.ui.browser;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import l.files.fs.File;
import l.files.ui.bookmarks.BookmarksFragment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.browser.Instrumentations.awaitOnMainThread;
import static l.files.ui.browser.Instrumentations.clickItemOnMainThread;
import static l.files.ui.browser.Instrumentations.findItemOnMainThread;
import static l.files.ui.browser.Instrumentations.longClickItemOnMainThread;

final class UiBookmarksFragment {

    private final UiFileActivity context;

    UiBookmarksFragment(UiFileActivity context) {
        this.context = requireNonNull(context);
    }

    UiFileActivity activityObject() {
        return context;
    }

    UiBookmarksFragment longClick(File bookmark) {
        longClickItemOnMainThread(context.instrumentation(), recycler(), bookmark);
        return this;
    }

    private BookmarksFragment fragment() {
        return (BookmarksFragment) context
                .activity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.bookmarks_fragment);
    }

    UiBookmarksFragment click(File bookmark) {
        clickItemOnMainThread(context.instrumentation(), recycler(), bookmark);
        return this;
    }

    UiBookmarksFragment delete() {
        UiFileActivity activity = activityObject();
        activity.selectActionModeAction(R.id.delete_selected_bookmarks);
        activity.waitForActionModeToFinish();
        return this;
    }

    UiBookmarksFragment assertBookmarked(
            final File bookmark,
            final boolean bookmarked) {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                assertEquals(bookmarked, fragment().bookmarks().contains(bookmark));
            }
        });
        return this;
    }

    UiBookmarksFragment assertActionModePresent(boolean present) {
        activityObject().assertActionModePresent(present);
        return this;
    }

    UiBookmarksFragment assertActionModeTitle(Object title) {
        activityObject().assertActionModeTitle(title);
        return this;
    }

    UiBookmarksFragment assertChecked(
            File bookmark, final boolean checked) {
        findItemOnMainThread(
                context.instrumentation(),
                recycler(),
                bookmark,
                new Consumer<View>() {
                    @Override
                    public void apply(View view) {
                        assertEquals(checked, view.isActivated());
                    }
                });
        return this;
    }

    private Provider<RecyclerView> recycler() {
        return new Provider<RecyclerView>() {
            @Override
            public RecyclerView get() {
                BookmarksFragment fragment = fragment();
                assertNotNull(fragment);
                return fragment.recycler;
            }
        };
    }

    UiBookmarksFragment assertDrawerIsOpened(boolean opened) {
        activityObject().assertDrawerIsOpened(opened);
        return this;
    }

    UiBookmarksFragment pressBack() {
        activityObject().pressBack();
        return this;
    }
}
