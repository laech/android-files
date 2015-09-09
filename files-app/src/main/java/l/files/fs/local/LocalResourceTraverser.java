package l.files.fs.local;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;

import l.files.fs.LinkOption;
import l.files.fs.Resource;
import l.files.fs.Stream;
import l.files.fs.Visitor;
import l.files.fs.Visitor.Result;

import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;

final class LocalResourceTraverser {

  private final Resource root;
  private final LinkOption rootOption;
  private final Visitor visitor;
  private final Deque<Node> stack;

  LocalResourceTraverser(Resource root, LinkOption option, Visitor visitor) {
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
          result = visitor.onPreVisit(node.resource);
        } catch (IOException e) {
          stack.pop();
          visitor.onException(node.resource, e);
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
          visitor.onException(node.resource, e);
        }

      } else {

        stack.pop();
        Result result;
        try {
          result = visitor.onPostVisit(node.resource);
        } catch (IOException e) {
          visitor.onException(node.resource, e);
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
    LinkOption option = parent.resource.equals(root) ? rootOption : NOFOLLOW;
    if (!parent.resource.stat(option).isDirectory()) {
      return;
    }

    try (Stream<Resource> stream = parent.resource.list(option)) {
      List<Resource> items = stream.to(new ArrayList<Resource>());
      ListIterator<Resource> it = items.listIterator(items.size());
      while (it.hasPrevious()) {
        stack.push(new Node(it.previous()));
      }
    }
  }

  private static class Node {
    final Resource resource;
    boolean visited;

    private Node(Resource resource) {
      this.resource = requireNonNull(resource, "resource");
    }
  }
}
