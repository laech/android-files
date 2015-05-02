package l.files.fs;

import java.io.IOException;

public interface Visitor
{
    /**
     * Callback method when traversing a resource subtree.
     */
    Result accept(Resource resource) throws IOException;

    enum Result
    {
        /**
         * Continue traversing.
         */
        CONTINUE,

        /**
         * Stop traversing immediately.
         */
        TERMINATE,

        /**
         * Stop receiving callback regarding the current subtree.
         */
        SKIP
    }
}
