package l.files.operations;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.File;
import l.files.fs.local.LocalFile;

import static java.util.Arrays.asList;

public final class TargetTest extends TestCase {

    public void testCreate() throws Exception {
        Set<File> srcFiles = Collections.<File>singleton(LocalFile.of("src"));
        File dstDir = LocalFile.of("dst");
        Target target = Target.from(srcFiles, dstDir);
        assertEquals(srcFiles, new HashSet<>(target.srcFiles()));
        assertEquals(dstDir, target.dstDir());
    }

    public void testFromSource() throws Exception {
        Target target = Target.from(asList(
                LocalFile.of("/0/a/b"),
                LocalFile.of("/0/a/c")
        ));
        assertEquals(LocalFile.of("/0/a"), target.dstDir());
    }

}
