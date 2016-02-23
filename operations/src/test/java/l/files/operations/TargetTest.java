package l.files.operations;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import l.files.fs.Name;
import l.files.fs.Path;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public final class TargetTest {

    @Test
    public void create() throws Exception {
        Set<Name> sourceFiles = singleton(mock(Name.class));
        Path sourceDirectory = mock(Path.class);
        Path destinationDirectory = mock(Path.class);
        Target target = Target.from(sourceDirectory, sourceFiles, destinationDirectory);
        assertEquals(sourceFiles, new HashSet<>(target.srcFiles()));
        assertEquals(sourceDirectory, target.sourceDirectory());
        assertEquals(destinationDirectory, target.destinationDirectory());
    }

    @Test
    public void create_from_source() throws Exception {
        Path sourceDirectory = mock(Path.class);
        Name child1 = mock(Name.class, "/0/a/b");
        Name child2 = mock(Name.class, "/0/a/c");
        Target target = Target.from(sourceDirectory, asList(child1, child2));
        assertEquals(sourceDirectory, target.sourceDirectory());
        assertEquals(sourceDirectory, target.destinationDirectory());
    }

}
