package l.files.operations;

import org.junit.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class TargetTest {

    @Test
    public void create() {
        Set<Path> srcPaths = singleton(mock(Path.class));
        Path dstDir = mock(Path.class);
        Target target = Target.from(srcPaths, dstDir);
        assertEquals(srcPaths, new HashSet<>(target.srcFiles()));
        assertEquals(dstDir, target.dstDir());
    }

    @Test
    public void create_from_source() {
        Path parent = mock(Path.class, "/0/a");
        Path child1 = mock(Path.class, "/0/a/b");
        Path child2 = mock(Path.class, "/0/a/c");
        given(child1.getParent()).willReturn(parent);
        given(child2.getParent()).willReturn(parent);
        Target target = Target.from(asList(child1, child2));
        assertEquals(parent, target.dstDir());
    }

}
