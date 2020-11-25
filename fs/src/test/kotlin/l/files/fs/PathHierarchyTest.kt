package l.files.fs

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.nio.file.Paths

@RunWith(Parameterized::class)
class PathHierarchyTest(
  path: String,
  expectedHierarchy: List<String>
) {

  private val path = Paths.get(path)
  private val expectedHierarchy = expectedHierarchy.map(Paths::get)

  @Test
  fun test() {
    assertEquals(expectedHierarchy, path.hierarchy())
  }

  companion object {
    @JvmStatic
    @Parameters(name = "\"{0}\".hierarchy() == \"{1}\"")
    fun paths() = listOf(
      arrayOf("", listOf("")),
      arrayOf("/", listOf("/")),
      arrayOf("/a/b/c", listOf("/a/b/c", "/a/b", "/a", "/"))
    )
  }
}
