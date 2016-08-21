package l.files.ui.preview;

import static l.files.base.Objects.requireNonNull;

class NoPreview {

    static final NoPreview NOT_REGULAR_FILE = new NoPreview("not a regular file");
    static final NoPreview FILE_UNREADABLE = new NoPreview("file is unreadable");
    static final NoPreview IN_NO_PREVIEW_CACHE = new NoPreview("file marked in no preview cache");
    static final NoPreview PATH_IN_CACHE_DIR = new NoPreview("path is in cache directory");
    static final NoPreview DECODE_RETURNED_NULL = new NoPreview("decode returned null");

    final Object cause;

    NoPreview(Object cause) {
        this.cause = requireNonNull(cause, "cause");
    }

}
