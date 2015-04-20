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
import l.files.fs.ResourceVisitor.ExceptionHandler;
import l.files.fs.ResourceVisitor.Result;
import l.files.fs.UncheckedIOException;

import static java.util.Objects.requireNonNull;
import static l.files.fs.ResourceVisitor.Order.POST;
import static l.files.fs.ResourceVisitor.Order.PRE;

/**
 * Conforms to the contract of {@link Resource#traverse(ResourceVisitor)}.
 */
final class LocalResourceTraverser {

    private final ResourceVisitor visitor;
    private final ExceptionHandler handler;
    private final Deque<Node> stack;

    LocalResourceTraverser(Resource root,
                           ResourceVisitor visitor,
                           ExceptionHandler handler) {
        requireNonNull(root, "root");
        this.visitor = requireNonNull(visitor, "visitor");
        this.handler = requireNonNull(handler, "handler");
        this.stack = new ArrayDeque<>();
        this.stack.push(new Node(root));
    }

    void traverse() throws IOException {

        while (!stack.isEmpty()) {

            Node node = stack.peek();
            if (!node.visited) {
                node.visited = true;

                Result result;
                try {
                    result = visitor.accept(PRE, node.resource);
                } catch (IOException e) {
                    stack.pop();
                    handler.handle(PRE, node.resource, e);
                    continue;
                }

                switch (result) {
                    case TERMINATE:
                        return;
                    case SKIP:
                        stack.pop();
                        continue;
                }

                try {
                    pushChildren(stack, node);
                } catch (UncheckedIOException e) {
                    handler.handle(PRE, node.resource, e.getCause());
                } catch (IOException e) {
                    handler.handle(PRE, node.resource, e);
                }

            } else {

                stack.pop();
                Result result;
                try {
                    result = visitor.accept(POST, node.resource);
                } catch (IOException e) {
                    handler.handle(POST, node.resource, e);
                    continue;
                }
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
