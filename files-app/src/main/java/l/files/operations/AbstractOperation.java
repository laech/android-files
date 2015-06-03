package l.files.operations;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;

import l.files.fs.ExceptionHandler;
import l.files.fs.Resource;
import l.files.fs.Visitor;

import static java.lang.Thread.currentThread;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.TERMINATE;

abstract class AbstractOperation implements FileOperation
{

    /**
     * The amount of errors to catch before stopping. Don't want to hold an
     * endless amount of errors (resulting in OutOfMemoryError). And there is
     * not much point of continuing if number of errors reached this amount.
     */
    private static final int ERROR_LIMIT = 20;

    private final Iterable<Resource> resources;
    private final FailureRecorder recorder;

    AbstractOperation(final Iterable<? extends Resource> resources)
    {
        this.resources = ImmutableSet.copyOf(resources);
        this.recorder = new FailureRecorder(ERROR_LIMIT);
    }

    final void checkInterrupt() throws InterruptedException
    {
        if (Thread.interrupted())
        {
            throw new InterruptedException();
        }
    }

    final boolean isInterrupted()
    {
        return currentThread().isInterrupted();
    }

    final void record(final Resource resource, final IOException exception)
    {
        recorder.onFailure(resource, exception);
    }

    final void preOrderTraversal(
            final Resource resource,
            final Visitor visitor) throws IOException
    {
        resource.traverse(
                NOFOLLOW,
                visitor,
                terminateOnInterrupt(),
                recordOnException());
    }

    final void postOrderTraversal(
            final Resource resource,
            final Visitor visitor) throws IOException
    {
        resource.traverse(NOFOLLOW,
                terminateOnInterrupt(),
                visitor,
                recordOnException());
    }

    private Visitor terminateOnInterrupt()
    {
        return new Visitor()
        {
            @Override
            public Result accept(final Resource resource) throws IOException
            {
                if (isInterrupted())
                {
                    return TERMINATE;
                }
                return CONTINUE;
            }
        };
    }

    private ExceptionHandler recordOnException()
    {
        return new ExceptionHandler()
        {
            @Override
            public void handle(
                    final Resource resource,
                    final IOException e) throws IOException
            {
                record(resource, e);
            }
        };
    }

    @Override
    public void execute() throws InterruptedException
    {
        for (final Resource resource : resources)
        {
            checkInterrupt();
            process(resource);
        }
        recorder.throwIfNotEmpty();
    }

    abstract void process(Resource resource) throws InterruptedException;

}
