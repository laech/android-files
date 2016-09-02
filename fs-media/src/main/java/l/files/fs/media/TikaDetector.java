package l.files.fs.media;

import android.content.Context;

import org.apache.tika.io.TaggedIOException;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.mime.MimeTypesFactory;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import l.files.fs.Path;
import l.files.fs.Stat;

/**
 * Detects the media type of the underlying file by reading it's header.
 */
abstract class TikaDetector extends BasePropertyDetector {

    @Nullable
    private static volatile MimeTypes types;

    @Override
    String detectFile(Context context, Path path, Stat stat) throws IOException {

        if (types == null) {
            synchronized (TikaDetector.class) {
                if (TikaDetector.types == null) {
                    try {
                        types = createMimeTypes(context);
                    } catch (MimeTypeException e) {
                        throw new IOException(e);
                    }
                }
            }
        }

        try {

            MimeTypes t = types;
            assert t != null;
            return detectFile(t, path);

        } catch (TaggedIOException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
    }

    private static MimeTypes createMimeTypes(Context context)
            throws IOException, MimeTypeException {
        /*
         * tika_mimetypes_xml_1_12 is a v1.12 of org.apache.tika.mime/tika-mimetypes.xml,
         * this is to work around the slowness of Android's Class.getResource*()
         * and to avoid the unnecessary memory usage increase because of the caching
         * used for the jar content created by Android's Class.getResource*().
         */
        InputStream in = context.getResources()
                .openRawResource(R.raw.tika_mimetypes_1_10);
        try {
            return MimeTypesFactory.create(in);
        } finally {
            in.close();
        }
    }

    abstract String detectFile(MimeTypes types, Path path) throws IOException;

}
