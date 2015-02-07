package l.files.operations

import java.io.IOException

import l.files.fs.Path

private class Move(src: Iterable<Path>, dst: Path) : Paste(src, dst) {

    volatile var movedItemCount = 0
        private set

    override fun paste(from: Path, to: Path, listener: FailureRecorder) {
        try {
            from.resource.move(to)
            movedItemCount++
        } catch (e: IOException) {
            listener.onFailure(from, e)
        }
    }

}
