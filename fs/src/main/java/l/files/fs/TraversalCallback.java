package l.files.fs;

import java.io.IOException;

import static l.files.fs.TraversalCallback.Result.CONTINUE;

public interface TraversalCallback<E> { // TODO make non-generic?

    Result onPreVisit(E element) throws IOException;

    Result onPostVisit(E element) throws IOException;

    void onException(E element, IOException e) throws IOException;

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

    class Base<E> implements TraversalCallback<E> {

        @Override
        public Result onPreVisit(E element) throws IOException {
            return CONTINUE;
        }

        @Override
        public Result onPostVisit(E element) throws IOException {
            return CONTINUE;
        }

        @Override
        public void onException(E element, IOException e) throws IOException {
            throw e;
        }

    }

}
