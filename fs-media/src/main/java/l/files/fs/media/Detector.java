package l.files.fs.media;

import android.content.Context;

import org.apache.tika.io.TaggedIOException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.mime.MimeTypesFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import l.files.base.BiFunction;
import l.files.fs.Path;
import l.files.fs.Stat;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.media.MediaTypes.MEDIA_TYPE_OCTET_STREAM;
import static org.apache.tika.metadata.TikaMetadataKeys.RESOURCE_NAME_KEY;

final class Detector {

    static final Detector INSTANCE = new Detector();

    // Media types for file types, kept consistent with the linux "file" command
    private static final String INODE_DIRECTORY = "inode/directory";
    private static final String INODE_BLOCKDEVICE = "inode/blockdevice";
    private static final String INODE_CHARDEVICE = "inode/chardevice";
    private static final String INODE_FIFO = "inode/fifo";
    private static final String INODE_SOCKET = "inode/socket";

    @Nullable
    private static volatile MimeTypes types;

    private Detector() {
    }

    String detect(Context context, Path path) throws IOException {
        return detect(context, path, path.stat(FOLLOW));
    }

    String detect(Context context, Path path, Stat stat) throws IOException {
        if (stat.isSymbolicLink()) {
            return detect(
                    context,
                    path.readSymbolicLink(),
                    path.stat(FOLLOW));
        }
        if (stat.isRegularFile()) return detectFile(context, path);
        if (stat.isFifo()) return INODE_FIFO;
        if (stat.isSocket()) return INODE_SOCKET;
        if (stat.isDirectory()) return INODE_DIRECTORY;
        if (stat.isBlockDevice()) return INODE_BLOCKDEVICE;
        if (stat.isCharacterDevice()) return INODE_CHARDEVICE;
        return MEDIA_TYPE_OCTET_STREAM;
    }

    private static String detectFile(
            Context context,
            Path path) throws IOException {

        if (types == null) {
            synchronized (Detector.class) {
                if (types == null) {
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

    private static String detectFile(MimeTypes types, Path path) throws IOException {

        Metadata meta = path.getName().fold(new Metadata(), new BiFunction<Metadata, String, Metadata>() {
            @Override
            public Metadata apply(Metadata meta, String name) {
                meta.add(RESOURCE_NAME_KEY, name);
                return meta;
            }
        });
        InputStream in = new BufferedInputStream(path.newInputStream());
        try {
            return types.detect(in, meta).getBaseType().toString();
        } finally {
            in.close();
        }
    }

}
