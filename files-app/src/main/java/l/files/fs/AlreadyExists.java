package l.files.fs;

public class AlreadyExists extends ResourceException
{
    public AlreadyExists(final String message)
    {
        super(message);
    }

    public AlreadyExists(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
