package l.files.ui.operations.actions;

import android.content.ClipboardManager;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.File;

import static android.content.ClipData.newIntent;
import static android.content.ClipData.newPlainText;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

final class Clipboards {

    private static final String ACTION_CUT = "l.files.intent.action.CUT";
    private static final String ACTION_COPY = "l.files.intent.action.COPY";
    private static final String EXTRA_FILES = "l.files.intent.extra.FILES";

    private Clipboards() {
    }

    static void clear(ClipboardManager manager) {
        manager.setPrimaryClip(newPlainText("", ""));
    }

    static boolean hasClip(ClipboardManager manager) {
        String action = getAction(manager);
        return ACTION_CUT.equals(action)
                || ACTION_COPY.equals(action);
    }

    static boolean isCut(ClipboardManager manager) {
        return ACTION_CUT.equals(getAction(manager));
    }

    static boolean isCopy(ClipboardManager manager) {
        return ACTION_COPY.equals(getAction(manager));
    }

    static Set<File> getFiles(ClipboardManager manager) {
        Intent intent = getClipboardIntent(manager);
        if (intent == null) {
            return emptySet();
        }
        intent.setExtrasClassLoader(Clipboards.class.getClassLoader());
        ArrayList<File> extras = intent.getParcelableArrayListExtra(EXTRA_FILES);
        if (extras == null) {
            return emptySet();
        }
        return unmodifiableSet(new HashSet<>(extras));
    }

    private static Intent getClipboardIntent(ClipboardManager manager) {
        try {
            return manager.getPrimaryClip().getItemAt(0).getIntent();
        } catch (NullPointerException e) {
            return null;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private static String getAction(ClipboardManager manager) {
        Intent intent = getClipboardIntent(manager);
        if (intent == null) {
            return null;
        }
        return intent.getAction();
    }

    static void setCut(ClipboardManager manager, Collection<? extends File> files) {
        setClipData(manager, files, ACTION_CUT);
    }

    static void setCopy(ClipboardManager manager, Collection<? extends File> files) {
        setClipData(manager, files, ACTION_COPY);
    }

    private static void setClipData(
            ClipboardManager manager,
            Collection<? extends File> files,
            String action) {
        Intent intent = new Intent(action);
        intent.putParcelableArrayListExtra(EXTRA_FILES, new ArrayList<>(files));
        manager.setPrimaryClip(newIntent(null, intent));
    }
}
