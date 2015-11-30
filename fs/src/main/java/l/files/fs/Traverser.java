package l.files.fs;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;

import l.files.fs.Visitor.Result;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;

final class Traverser {

    private final File root;
    private final LinkOption rootOption;
    private final Visitor visitor;
    private final Deque<Node> stack;
    private final Comparator<File> childrenComparator;

    Traverser(
            File root,
            LinkOption option,
            Visitor visitor,
            Comparator<File> childrenComparator) {

        this.childrenComparator = childrenComparator;
        this.rootOption = requireNonNull(option, "option");
        this.root = requireNonNull(root, "root");
        this.visitor = requireNonNull(visitor);
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
                    result = visitor.onPreVisit(node.file);
                } catch (IOException e) {
                    stack.pop();
                    visitor.onException(node.file, e);
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
                    visitor.onException(node.file, e);
                }

            } else {

                stack.pop();
                Result result;
                try {
                    result = visitor.onPostVisit(node.file);
                } catch (IOException e) {
                    visitor.onException(node.file, e);
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
        LinkOption option = parent.file.equals(root) ? rootOption : NOFOLLOW;
        if (!parent.file.stat(option).isDirectory()) {
            return;
        }

        List<File> children = parent.file.list(option, new ArrayList<File>());
        if (childrenComparator != null) {
            Collections.sort(children, childrenComparator);
        }

        ListIterator<File> it = children.listIterator(children.size());
        while (it.hasPrevious()) {
            stack.push(new Node(it.previous()));
        }
    }

    private static class Node {
        final File file;
        boolean visited;

        private Node(File file) {
            this.file = requireNonNull(file, "file");
        }
    }
}
