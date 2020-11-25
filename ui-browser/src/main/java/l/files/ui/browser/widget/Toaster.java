package l.files.ui.browser.widget;

import android.content.Context;

import java.util.function.Consumer;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static java.util.Objects.requireNonNull;

public final class Toaster implements Consumer<String> {

    private final Context context;

    public Toaster(Context context) {
        this.context = requireNonNull(context, "context");
    }

    @Override
    public void accept(String message) {
        makeText(context, message, LENGTH_SHORT).show();
    }
}
