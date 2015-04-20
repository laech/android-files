package l.files.fs;

import java.io.IOException;

public interface ResourceVisitor {

    /**
     * Callback method when traversing a resource subtree.
     * <p/>
     * e.g. traversing the follow tree:
     * <pre>
     *     a
     *    / \
     *   b   c
     * </pre>
     * will generate:
     * <pre>
     * visitor.accept(PRE, a)
     * visitor.accept(PRE, b)
     * visitor.accept(POST, b)
     * visitor.accept(PRE, c)
     * visitor.accept(POST, c)
     * visitor.accept(POST, a)
     * </pre>
     */
    Result accept(Order order, Resource resource) throws IOException;

    enum Result {

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

    enum Order {

        /**
         * Current callback is from the pre order traversal.
         */
        PRE,

        /**
         * Current callback is from the post order traversal.
         */
        POST
    }

    interface ExceptionHandler {

        /**
         * Callback method invoked when one of the resource failed to be visited
         * or {@link ResourceVisitor#accept(Order, Resource)} throws an
         * IOException. Any exception thrown from this method will terminate the
         * traversal the exception itself will be propagated through, if no
         * exception is thrown traversal will proceed.
         */
        void handle(Order order, Resource resource, IOException e)
                throws IOException;

    }

}
