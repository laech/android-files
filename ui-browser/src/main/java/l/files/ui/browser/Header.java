package l.files.ui.browser;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class Header {

    Header() {
    }

    abstract String header();

    static Header of(String header) {
        return new AutoValue_Header(header);
    }

    @Override
    public String toString() {
        return header();
    }
}
