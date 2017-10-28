package l.files.ui.browser;

import android.support.v7.widget.RecyclerView;

import l.files.fs.Path;
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

    UiBookmarksFragment longClick(Path bookmark) {
        longClickItemOnMainThread(context.instrumentation(), this::recycler, bookmark);
        return this;
    }

    private BookmarksFragment fragment() {
        return (BookmarksFragment) context
                .activity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.bookmarks_fragment);
    }

    UiBookmarksFragment click(Path bookmark) {
        assertDrawerIsOpened(true);
        clickItemOnMainThread(context.instrumentation(), this::recycler, bookmark);
        return this;
    }

    UiBookmarksFragment delete() {
        UiFileActivity activity = activityObject();
        activity.selectActionModeAction(R.id.delete_selected_bookmarks);
        activity.waitForActionModeToFinish();
        return this;
    }

    UiBookmarksFragment assertBookmarked(
            final Path bookmark,
            final boolean bookmarked) {
        awaitOnMainThread(context.instrumentation(), () ->
                assertEquals(bookmarked, fragment().bookmarks().contains(bookmark)));
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
            Path bookmark, final boolean checked) {
        findItemOnMainThread(
                context.instrumentation(),
                this::recycler,
                bookmark,
                view -> assertEquals(checked, view.isActivated()));
        return this;
    }

    private RecyclerView recycler() {
        BookmarksFragment fragment = fragment();
        assertNotNull(fragment);
        return fragment.recycler;
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
