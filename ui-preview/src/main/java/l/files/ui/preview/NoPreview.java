package l.files.ui.preview;

import android.support.annotation.Nullable;

class NoPreview {

    static final NoPreview FAILURE_UNAVAILABLE = new NoPreview(null);

    @Nullable
    final Throwable failure;

    NoPreview(@Nullable Throwable failure) {
        this.failure = failure;
    }

}
