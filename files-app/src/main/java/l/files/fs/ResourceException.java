package l.files.fs;

import java.io.IOException;

public class ResourceException extends IOException
{
    public ResourceException(final String message)
    {
        super(message);
    }

    public ResourceException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
