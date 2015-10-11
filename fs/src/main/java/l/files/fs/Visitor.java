package l.files.fs;

import java.io.IOException;

import static l.files.fs.Visitor.Result.CONTINUE;

public interface Visitor {

    Result onPreVisit(File file) throws IOException;

    Result onPostVisit(File file) throws IOException;

    void onException(File file, IOException e) throws IOException;

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

    class Base implements Visitor {

        @Override
        public Result onPreVisit(File file) throws IOException {
            return CONTINUE;
        }

        @Override
        public Result onPostVisit(File file) throws IOException {
            return CONTINUE;
        }

        @Override
        public void onException(File file, IOException e) throws IOException {
            throw e;
        }

    }

}