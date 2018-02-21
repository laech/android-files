package l.files.ui.browser;

import static l.files.base.Objects.requireNonNull;

public final class Header {

    private final String value;

    public Header(String value) {
        this.value = requireNonNull(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Header &&
                value.equals(((Header) o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
