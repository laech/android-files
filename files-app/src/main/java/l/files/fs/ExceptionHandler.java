package l.files.fs;

import java.io.IOException;

public interface ExceptionHandler
{
    /**
     * Callback method invoked when one of the resource failed to be visited or
     * {@link Visitor#accept(Resource)} throws an IOException. Any exception
     * thrown from this method will terminate the traversal the exception itself
     * will be propagated through, if no exception is thrown traversal will
     * proceed.
     */
    void handle(Resource resource, IOException e) throws IOException;
}
