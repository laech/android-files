package l.files.fs;

import java.io.IOException;

public final class Files {

    private Files() {
    }

    /**
     * Performs a depth first traverse of this tree.
     * <p/>
     * e.g. traversing the follow tree:
     * <pre>
     *     a
     *    / \
     *   b   c
     * </pre>
     * will generate:
     * <pre>
     * visitor.onPreVisit(a)
     * visitor.onPreVisit(b)
     * visitor.onPost(b)
     * visitor.onPreVisit(c)
     * visitor.onPost(c)
     * visitor.onPost(a)
     * </pre>
     *
     * @param option applies to root only, child links are never followed
     */
    public static void traverse(
            File file,
            LinkOption option,
            Visitor visitor) throws IOException {

        new Traverser(file, option, visitor).traverse();
    }

}
