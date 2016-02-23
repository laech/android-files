package l.files.fs;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;

import l.files.fs.TraversalCallback.Result;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;

final class Traverser {

    private final Path root;
    private final LinkOption rootOption;
    private final TraversalCallback<Path> visitor;
    private final Deque<Node> stack;
    private final Comparator<Name> childrenComparator;

    @SuppressWarnings("unchecked")
    Traverser(
            Path root,
            LinkOption option,
            TraversalCallback<? super Path> visitor,
            Comparator<? super Name> childrenComparator) {

        this.childrenComparator = (Comparator<Name>) childrenComparator;
        this.rootOption = requireNonNull(option, "option");
        this.root = requireNonNull(root, "root");
        this.visitor = (TraversalCallback<Path>) requireNonNull(visitor);
        this.stack = new ArrayDeque<>();
        this.stack.push(new RootNode(root));
    }

    void traverse() throws IOException {
        while (!stack.isEmpty()) {
            Node node = stack.peek();
            Path path = node.path();

            if (!node.visited) {
                node.visited = true;

                Result result;
                try {
                    result = visitor.onPreVisit(path);
                } catch (IOException e) {
                    stack.pop();
                    visitor.onException(path, e);
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
                    pushChildren(stack, path);
                } catch (IOException e) {
                    visitor.onException(path, e);
                }

            } else {

                stack.pop();
                Result result;
                try {
                    result = visitor.onPostVisit(path);
                } catch (IOException e) {
                    visitor.onException(path, e);
                    continue;
                }
                switch (result) {
                    case TERMINATE:
                        return;
                }

            }
        }
    }

    private void pushChildren(Deque<Node> stack, Path parent) throws IOException {
        LinkOption option = parent.equals(root) ? rootOption : NOFOLLOW;
        if (!Files.stat(parent, option).isDirectory()) { // TODO catch NotDirectory
            return;
        }

        List<Name> children = Files.list(parent, option, new ArrayList<Name>());
        if (childrenComparator != null) {
            Collections.sort(children, childrenComparator);
        }

        ListIterator<Name> it = children.listIterator(children.size());
        while (it.hasPrevious()) {
            stack.push(new ChildNode(parent, it.previous()));
        }
    }

    private static abstract class Node {
        boolean visited;

        abstract Path path();
    }

    private static final class RootNode extends Node {
        final Path path;

        RootNode(Path path) {
            this.path = path;
        }

        @Override
        Path path() {
            return path;
        }
    }

    private static class ChildNode extends Node {
        final Path parent;
        final Name name;

        ChildNode(Path parent, Name name) {
            this.parent = requireNonNull(parent);
            this.name = requireNonNull(name);
        }

        @Override
        Path path() {
            return parent.resolve(name);
        }
    }
}
