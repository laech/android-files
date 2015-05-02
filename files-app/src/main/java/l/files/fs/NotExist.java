package l.files.fs;

public class NotExist extends ResourceException
{
    public NotExist(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
