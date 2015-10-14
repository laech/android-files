package l.files.ui;

import android.content.Context;

import l.files.common.base.Consumer;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static java.util.Objects.requireNonNull;

public final class Toaster implements Consumer<String> {
    private final Context context;

    public Toaster(final Context context) {
        this.context = requireNonNull(context, "context");
    }

    @Override
    public void apply(final String message) {
        makeText(context, message, LENGTH_SHORT).show();
    }
}
