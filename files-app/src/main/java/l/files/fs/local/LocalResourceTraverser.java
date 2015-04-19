package l.files.fs.local;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;

import l.files.fs.NotDirectoryException;
import l.files.fs.Resource;
import l.files.fs.Resource.Stream;
import l.files.fs.ResourceVisitor;
import l.files.fs.ResourceVisitor.Result;

import static java.util.Objects.requireNonNull;
import static l.files.fs.ResourceVisitor.Order.POST;
import static l.files.fs.ResourceVisitor.Order.PRE;

/**
 * Conforms to the contract of {@link Resource#traverse(ResourceVisitor)}.
 */
final class LocalResourceTraverser {

    private final Resource root;

    LocalResourceTraverser(Resource root) {
        this.root = requireNonNull(root, "root");
    }

    void traverse(ResourceVisitor visitor) throws IOException {
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(new Node(root));
        while (!stack.isEmpty()) {
            Node node = stack.peek();
            if (!node.visited) {
                node.visited = true;

                Result result = visitor.accept(PRE, node.resource);
                switch (result) {
                    case TERMINATE:
                        return;
                    case SKIP:
                        stack.pop();
                        continue;
                }
                pushChildren(stack, node);

            } else {
                stack.pop();
                Result result = visitor.accept(POST, node.resource);
                switch (result) {
                    case TERMINATE:
                        return;
                }
            }

        }
    }

    private void pushChildren(Deque<Node> stack, Node parent)
            throws IOException {
        try (Stream stream = parent.resource.openDirectory()) {
            List<Node> nodes = new ArrayList<>();
            for (Resource child : stream) {
                nodes.add(new Node(child));
            }
            ListIterator<Node> it = nodes.listIterator(nodes.size());
            while (it.hasPrevious()) {
                stack.push(it.previous());
            }
        } catch (NotDirectoryException ignore) {
        }
    }

    private static final class Node {
        final Resource resource;
        boolean visited;

        private Node(Resource resource) {
            this.resource = requireNonNull(resource, "resource");
        }
    }
}
