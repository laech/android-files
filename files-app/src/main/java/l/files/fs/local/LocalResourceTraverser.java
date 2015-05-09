package l.files.fs.local;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nullable;

import l.files.fs.ExceptionHandler;
import l.files.fs.LinkOption;
import l.files.fs.NotDirectory;
import l.files.fs.Resource;
import l.files.fs.Visitor;
import l.files.fs.Visitor.Result;

import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;

final class LocalResourceTraverser
{

    private static final Visitor DEFAULT_VISITOR = new Visitor()
    {
        @Override
        public Result accept(final Resource resource) throws IOException
        {
            return CONTINUE;
        }
    };

    private static final ExceptionHandler DEFAULT_HANDLER = new ExceptionHandler()
    {
        @Override
        public void handle(
                final Resource resource,
                final IOException e) throws IOException
        {
            throw e;
        }
    };

    private final Resource root;
    private final LinkOption rootOption;
    private final Visitor pre;
    private final Visitor post;
    private final ExceptionHandler handler;
    private final Deque<Node> stack;

    LocalResourceTraverser(
            final Resource root,
            final LinkOption option,
            @Nullable final Visitor pre,
            @Nullable final Visitor post,
            @Nullable final ExceptionHandler handler)
    {
        this.rootOption = requireNonNull(option, "option");
        this.root = requireNonNull(root, "root");
        this.pre = pre != null ? pre : DEFAULT_VISITOR;
        this.post = post != null ? post : DEFAULT_VISITOR;
        this.handler = handler != null ? handler : DEFAULT_HANDLER;
        this.stack = new ArrayDeque<>();
        this.stack.push(new Node(root));
    }

    void traverse() throws IOException
    {
        while (!stack.isEmpty())
        {
            final Node node = stack.peek();
            if (!node.visited)
            {
                node.visited = true;

                final Result result;
                try
                {
                    result = pre.accept(node.resource);
                }
                catch (final IOException e)
                {
                    stack.pop();
                    handler.handle(node.resource, e);
                    continue;
                }

                switch (result)
                {
                    case TERMINATE:
                        return;
                    case SKIP:
                        stack.pop();
                        continue;
                }

                try
                {
                    pushChildren(stack, node);
                }
                catch (final IOException e)
                {
                    handler.handle(node.resource, e);
                }
            }
            else
            {
                stack.pop();
                final Result result;
                try
                {
                    result = post.accept(node.resource);
                }
                catch (final IOException e)
                {
                    handler.handle(node.resource, e);
                    continue;
                }
                switch (result)
                {
                    case TERMINATE:
                        return;
                }
            }
        }
    }

    private void pushChildren(final Deque<Node> stack, final Node parent)
            throws IOException
    {

        final LinkOption option =
                parent.resource.equals(root) ? rootOption : NOFOLLOW;
        try
        {
            final List<Resource> children = parent.resource.list(option);
            final ListIterator<Resource> it = children.listIterator(children.size());
            while (it.hasPrevious())
            {
                stack.push(new Node(it.previous()));
            }
        }
        catch (final NotDirectory ignore)
        {
        }
    }

    private static final class Node
    {
        final Resource resource;
        boolean visited;

        private Node(final Resource resource)
        {
            this.resource = requireNonNull(resource, "resource");
        }
    }
}
