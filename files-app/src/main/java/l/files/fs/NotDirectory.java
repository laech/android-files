package l.files.fs;

public class NotDirectory extends ResourceException
{
    public NotDirectory(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
