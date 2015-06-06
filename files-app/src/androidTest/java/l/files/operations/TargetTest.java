package l.files.operations;

import junit.framework.TestCase;

import java.io.File;

import l.files.fs.local.LocalResource;

import static java.util.Arrays.asList;

public final class TargetTest extends TestCase
{

    public void testCreate() throws Exception
    {
        final Target target = Target.create("src", "dst");
        assertEquals("src", target.source().toString());
        assertEquals("dst", target.destination().toString());
    }

    public void testFromSource() throws Exception
    {
        final Target target = Target.from(asList(
                LocalResource.create(new File("/0/a/b")),
                LocalResource.create(new File("/0/a/c"))
        ));
        assertEquals("a", target.source().toString());
        assertEquals("a", target.destination().toString());
    }

    public void testFromSourceAndDestination() throws Exception
    {
        final Target target = Target.from(
                asList(
                        LocalResource.create(new File("/0/a/b")),
                        LocalResource.create(new File("/0/a/c"))
                ),
                LocalResource.create(new File("/a/b/c/d"))
        );
        assertEquals("a", target.source().toString());
        assertEquals("d", target.destination().toString());
    }

}
