package l.files.fs.local;

import org.apache.tika.Tika;

final class TikaHolder {

    private TikaHolder() {
    }

    static final Tika tika = new Tika();

}
