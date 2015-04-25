package l.files.fs.local;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nullable;

import l.files.fs.NotDirectoryException;
import l.files.fs.Resource;
import l.files.fs.ResourceExceptionHandler;
import l.files.fs.ResourceVisitor;
import l.files.fs.ResourceVisitor.Result;
import l.files.fs.UncheckedIOException;

import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.ResourceVisitor.Result.CONTINUE;

final class LocalResourceTraverser {

    private static final ResourceVisitor DEFAULT_VISITOR =
            new ResourceVisitor() {
                @Override
                public Result accept(Resource resource) throws IOException {
                    return CONTINUE;
                }
            };

    private static final ResourceExceptionHandler DEFAULT_HANDLER =
            new ResourceExceptionHandler() {
                @Override
                public void handle(Resource resource, IOException e)
                        throws IOException {
                    throw e;
                }
            };

    private final ResourceVisitor pre;
    private final ResourceVisitor post;
    private final ResourceExceptionHandler handler;
    private final Deque<Node> stack;

    LocalResourceTraverser(Resource root,
                           @Nullable ResourceVisitor pre,
                           @Nullable ResourceVisitor post,
                           @Nullable ResourceExceptionHandler handler) {
        requireNonNull(root, "root");
        this.pre = pre != null ? pre : DEFAULT_VISITOR;
        this.post = post != null ? post : DEFAULT_VISITOR;
        this.handler = handler != null ? handler : DEFAULT_HANDLER;
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
                    result = pre.accept(node.resource);
                } catch (IOException e) {
                    stack.pop();
                    handler.handle(node.resource, e);
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
                    handler.handle(node.resource, e.getCause());
                } catch (IOException e) {
                    handler.handle(node.resource, e);
                }

            } else {

                stack.pop();
                Result result;
                try {
                    result = post.accept(node.resource);
                } catch (IOException e) {
                    handler.handle(node.resource, e);
                    continue;
                }
                switch (result) {
                    case TERMINATE:
                        return;
                }

            }

        }
    }

    private void pushChildren(final Deque<Node> stack, Node parent)
            throws IOException {

        try {
            List<Resource> children = parent.resource.list(NOFOLLOW);
            ListIterator<Resource> it = children.listIterator(children.size());
            while (it.hasPrevious()) {
                stack.push(new Node(it.previous()));
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
