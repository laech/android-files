package l.files.ui.browser;

import androidx.recyclerview.widget.RecyclerView;
import l.files.fs.Path;
import l.files.ui.bookmarks.BookmarksFragment;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.browser.Instrumentations.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

final class UiBookmarksFragment {

    private final UiFileActivity context;

    UiBookmarksFragment(UiFileActivity context) {
        this.context = requireNonNull(context);
    }

    UiFileActivity activityObject() {
        return context;
    }

    UiBookmarksFragment longClick(Path bookmark) {
        longClickItemOnMainThread(
            context.getInstrumentation(),
            this::recycler,
            bookmark
        );
        return this;
    }

    private BookmarksFragment fragment() {
        return (BookmarksFragment) context
            .getActivity()
            .getSupportFragmentManager()
            .findFragmentById(R.id.bookmarks_fragment);
    }

    UiBookmarksFragment click(Path bookmark) {
        assertDrawerIsOpened(true);
        clickItemOnMainThread(
            context.getInstrumentation(),
            this::recycler,
            bookmark
        );
        return this;
    }

    UiBookmarksFragment delete() {
        UiFileActivity activity = activityObject();
        activity.selectActionModeAction(R.id.delete_selected_bookmarks);
        activity.waitForActionModeToFinish();
        return this;
    }

    UiBookmarksFragment assertBookmarked(Path bookmark, boolean bookmarked) {
        awaitOnMainThread(context.getInstrumentation(), () ->
            assertEquals(
                bookmarked,
                fragment().bookmarks().contains(bookmark)
            ));
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

    UiBookmarksFragment assertChecked(Path bookmark, boolean checked) {
        findItemOnMainThread(
            context.getInstrumentation(),
            this::recycler,
            bookmark,
            view -> {
                assertEquals(checked, view.isActivated());
                return null;
            }
        );
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
