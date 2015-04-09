package l.files.operations;

import junit.framework.TestCase;

import java.io.File;

import l.files.fs.local.LocalResource;

import static java.util.Arrays.asList;

public final class TargetTest extends TestCase {

    public void testCreate() throws Exception {
        Target target = Target.create("src", "dst");
        assertEquals("src", target.getSource());
        assertEquals("dst", target.getDestination());
    }

    public void testFromSource() throws Exception {
        Target target = Target.from(asList(
                LocalResource.create(new File("/0/a/b")),
                LocalResource.create(new File("/0/a/c"))
        ));
        assertEquals("a", target.getSource());
        assertEquals("a", target.getDestination());
    }

    public void testFromSourceAndDestination() throws Exception {
        Target target = Target.from(
                asList(
                        LocalResource.create(new File("/0/a/b")),
                        LocalResource.create(new File("/0/a/c"))
                ),
                LocalResource.create(new File("/a/b/c/d"))
        );
        assertEquals("a", target.getSource());
        assertEquals("d", target.getDestination());
    }

}
