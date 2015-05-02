package l.files.fs;

public class NotFile extends ResourceException
{
    public NotFile(final String message)
    {
        super(message);
    }

    public NotFile(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
