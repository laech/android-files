package l.files.fs.media;

import android.content.Context;

import org.apache.tika.io.TaggedIOException;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.mime.MimeTypesFactory;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;
import l.files.fs.Path;
import l.files.fs.Stat;

/**
 * Detects the media type of the underlying file by reading it's header.
 */
abstract class TikaDetector extends BasePropertyDetector {

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

        Closer closer = Closer.create();
        try {

            return detectFile(types, path, closer);

        } catch (TaggedIOException e) {
            if (e.getCause() != null) {
                throw closer.rethrow(e.getCause());
            } else {
                throw closer.rethrow(e);
            }
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
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

        Closer closer = Closer.create();
        try {
            InputStream in = closer.register(context.getResources()
                    .openRawResource(R.raw.tika_mimetypes_1_10));
            return MimeTypesFactory.create(in);
        }catch (Throwable e){
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    abstract String detectFile(MimeTypes types, Path path, Closer closer) throws IOException;

}
