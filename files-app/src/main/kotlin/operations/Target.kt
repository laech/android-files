package l.files.operations

import l.files.fs.Path
import kotlin.platform.platformStatic

/**
 * Source and destination of a file task.
 */
data class Target(

        /**
         * Name of the source file/directory the task is operating from.
         */
        val source: String,

        /**
         * Name of the destination file/directory the task is operating to.
         */
        val destination: String) {

    companion object {

        val NONE = Target("", "")

        platformStatic fun fromPaths(sources: Iterable<Path>, dst: Path): Target {
            val source = sources.iterator().next().parent!!.name
            val destination = dst.name
            return Target(source, destination)
        }

        platformStatic fun fromPaths(paths: Iterable<Path>): Target {
            val name = paths.iterator().next().parent!!.name
            return Target(name, name)
        }

    }

}
