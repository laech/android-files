package l.files.fs;

public class AccessDenied extends ResourceException
{
    public AccessDenied(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
