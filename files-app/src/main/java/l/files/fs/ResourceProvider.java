package l.files.fs;

import java.net.URI;

public interface ResourceProvider
{
    /**
     * @throws IllegalArgumentException
     *         if the URI scheme cannot be handled
     */
    Resource get(URI uri);
}
