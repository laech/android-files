package l.files.operations;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import l.files.fs.File;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class TargetTest {

    @Test
    public void create() throws Exception {
        Set<File> srcFiles = singleton(mock(File.class));
        File dstDir = mock(File.class);
        Target target = Target.from(srcFiles, dstDir);
        assertEquals(srcFiles, new HashSet<>(target.srcFiles()));
        assertEquals(dstDir, target.dstDir());
    }

    @Test
    public void create_from_source() throws Exception {
        File parent = mock(File.class, "/0/a");
        File child1 = mock(File.class, "/0/a/b");
        File child2 = mock(File.class, "/0/a/c");
        given(child1.parent()).willReturn(parent);
        given(child2.parent()).willReturn(parent);
        Target target = Target.from(asList(child1, child2));
        assertEquals(parent, target.dstDir());
    }

}
