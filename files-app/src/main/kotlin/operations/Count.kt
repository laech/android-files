package l.files.operations

import java.io.IOException

import l.files.fs.Path
import l.files.fs.Resource

import l.files.fs.Resource.TraversalOrder.BREATH_FIRST

private open class Count(paths: Iterable<Path>) : AbstractOperation(paths) {

    volatile var count: Int = 0
        private set

    override fun process(path: Path, listener: FailureRecorder) {
        try {
            count(path)
        } catch (e: IOException) {
            listener.onFailure(path, e)
        }
    }

    private fun count(path: Path, listener: FailureRecorder) {
        path.resource.traverse(BREATH_FIRST) { res, err ->
            listener.onFailure(res.path, err)
        }.forEach {
            checkInterrupt()
            onCount(it)
            count++
        }
    }

    open fun onCount(resource: Resource) {
    }

}
