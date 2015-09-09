package l.files.fs;

import java.io.IOException;

import static l.files.fs.Visitor.Result.CONTINUE;

public interface Visitor {

  Result onPreVisit(Resource res) throws IOException;

  Result onPostVisit(Resource res) throws IOException;

  void onException(Resource res, IOException e) throws IOException;

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

    @Override public Result onPreVisit(Resource res) throws IOException {
      return CONTINUE;
    }

    @Override public Result onPostVisit(Resource res) throws IOException {
      return CONTINUE;
    }

    @Override
    public void onException(Resource res, IOException e) throws IOException {
      throw e;
    }

  }

}
