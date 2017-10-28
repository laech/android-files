package l.files.fs;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nullable;

import l.files.fs.TraversalCallback.Result;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;

final class Traverser {

    private final Path root;
    private final LinkOption rootOption;
    private final TraversalCallback<Path> visitor;
    private final Deque<Node> stack;

    @Nullable
    private final Comparator<Path> childrenComparator;

    @SuppressWarnings("unchecked")
    Traverser(
            Path root,
            LinkOption option,
            TraversalCallback<? super Path> visitor,
            @Nullable Comparator<? super Path> childrenComparator
    ) {
        this.childrenComparator = (Comparator<Path>) childrenComparator;
        this.rootOption = requireNonNull(option, "option");
        this.root = requireNonNull(root, "root");
        this.visitor = (TraversalCallback<Path>) requireNonNull(visitor);
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
                    result = visitor.onPreVisit(node.path);
                } catch (IOException e) {
                    stack.pop();
                    visitor.onException(node.path, e);
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
                } catch (IOException e) {
                    visitor.onException(node.path, e);
                }

            } else {

                stack.pop();
                Result result;
                try {
                    result = visitor.onPostVisit(node.path);
                } catch (IOException e) {
                    visitor.onException(node.path, e);
                    continue;
                }
                switch (result) {
                    case TERMINATE:
                        return;
                }

            }
        }
    }

    private void pushChildren(Deque<Node> stack, Node parent) throws IOException {
        LinkOption option = parent.path.equals(root) ? rootOption : NOFOLLOW;
        if (!parent.path.stat(option).isDirectory()) {
            return;
        }

        List<Path> children = parent.path.list(new ArrayList<>());
        if (childrenComparator != null) {
            Collections.sort(children, childrenComparator);
        }

        ListIterator<Path> it = children.listIterator(children.size());
        while (it.hasPrevious()) {
            stack.push(new Node(it.previous()));
        }
    }

    private static class Node {
        final Path path;
        boolean visited;

        private Node(Path path) {
            this.path = requireNonNull(path);
        }
    }
}
