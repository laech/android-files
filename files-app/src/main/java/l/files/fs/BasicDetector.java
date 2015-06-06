package l.files.fs;

import android.webkit.MimeTypeMap;

import com.google.common.net.MediaType;

import java.io.IOException;

import static com.google.common.net.MediaType.OCTET_STREAM;

/**
 * Detects content type based on name and resource type.
 */
public final class BasicDetector extends AbstractDetector
{
    public static final BasicDetector INSTANCE = new BasicDetector();

    private BasicDetector()
    {
    }

    @Override
    protected MediaType detectFile(final Resource resource, final Stat stat)
            throws IOException
    {
        final MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        final String ext = resource.name().ext();
        final String type = typeMap.getMimeTypeFromExtension(ext);
        if (type == null)
        {
            return OCTET_STREAM;
        }
        try
        {
            return MediaType.parse(type);
        }
        catch (final IllegalArgumentException e)
        {
            return OCTET_STREAM;
        }
    }
}
