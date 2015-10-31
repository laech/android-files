package l.files.ui.browser;

import android.content.Context;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static l.files.base.Objects.requireNonNull;

final class Toaster implements Consumer<String> {
    private final Context context;

    Toaster(final Context context) {
        this.context = requireNonNull(context, "context");
    }

    @Override
    public void apply(final String message) {
        makeText(context, message, LENGTH_SHORT).show();
    }
}
