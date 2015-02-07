package l.files.fs

import l.files.common.testing.FileBaseTest

import junit.framework.Assert.*
import l.files.fs.Resource.TraversalOrder.BREATH_FIRST
import l.files.fs.local.toResourcePath

public class ResourceTest : FileBaseTest() {

    public fun testTraversal() {
        tmp().createFile("a/b")
        val f2 = tmp().createFile("a/c").toResourcePath()
        tmp().get("a/d").toResourcePath().resource.createSymbolicLink(f2)

        val expected = arrayListOf(
                tmp().get(),
                tmp().get("a"),
                tmp().get("a/b"),
                tmp().get("a/c"),
                tmp().get("a/d")
        ).map { it.getPath() }

        val actual = tmp().get().toResourcePath().resource.traverse(BREATH_FIRST, { r, e -> })
                .map { it.path.toString() }
                .toArrayList()

        assertEquals(expected, actual)
    }

}
