package l.files.fs;

import com.google.common.net.MediaType;

import org.apache.tika.Tika;
import org.apache.tika.io.TaggedIOException;

import java.io.IOException;
import java.io.InputStream;

import static com.google.common.net.MediaType.OCTET_STREAM;
import static l.files.fs.LinkOption.FOLLOW;

/**
 * Detects the media type of the underlying file by reading it's header.
 */
public final class MagicDetector extends AbstractDetector
{
    private static final class TikaHolder
    {
        static final Tika tika = new Tika();
    }

    public static final MagicDetector INSTANCE = new MagicDetector();

    private MagicDetector()
    {
    }

    @Override
    protected MediaType detectFile(final Resource resource, final Stat stat)
            throws IOException
    {
        try
        {
            try (InputStream in = resource.input(FOLLOW))
            {
                return MediaType.parse(TikaHolder.tika.detect(in));
            }
        }
        catch (final TaggedIOException e)
        {
            if (e.getCause() != null)
            {
                throw e.getCause();
            }
            else
            {
                throw e;
            }
        }
        catch (final IllegalArgumentException e)
        {
            return OCTET_STREAM;
        }
    }

}
