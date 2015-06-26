package l.files.fs.ResourceSpec;

import static junit.framework.Assert.fail;

final class Exceptions
{
    private Exceptions()
    {
    }

    static void expect(
            final Class<? extends Exception> clazz,
            final Code code) throws Exception
    {
        try
        {
            code.run();
            fail();
        }
        catch (final Exception e)
        {
            if (!clazz.isInstance(e))
            {
                throw e;
            }
        }
    }

    interface Code
    {
        void run() throws Exception;
    }

}
