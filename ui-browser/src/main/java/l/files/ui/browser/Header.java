package l.files.ui.browser;

import static l.files.base.Objects.requireNonNull;

final class Header {

    private final String header;

    private Header(String header) {
        this.header = requireNonNull(header);
    }

    String header() {
        return header;
    }

    static Header of(String header) {
        return new Header(header);
    }

    @Override
    public String toString() {
        return header();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Header &&
                header.equals(((Header) o).header);
    }

    @Override
    public int hashCode() {
        return header.hashCode();
    }
}
