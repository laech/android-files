package l.files.operations

import java.io.FileNotFoundException
import java.io.IOException

import l.files.fs.Path

import l.files.fs.Resource.TraversalOrder.POST_ORDER
import l.files.fs.Resource

class Delete(paths: Iterable<Path>) : AbstractOperation(paths) {

    volatile var deletedItemCount: Int = 0
        private set

    volatile var deletedByteCount: Long = 0
        private set

    override fun process(path: Path, listener: FailureRecorder) {
        try {
            deleteTree(path, listener)
        } catch (e: FileNotFoundException) {
            // Ignore
        } catch (e: IOException) {
            listener.onFailure(path, e)
        }
    }

    private fun deleteTree(path: Path, listener: FailureRecorder) {
        path.resource.traverse(POST_ORDER) { res, err ->
            listener.onFailure(res.path, err)
        }.forEach {
            checkInterrupt()
            try {
                delete(it)
            } catch (e: FileNotFoundException) {
                // Ignore
            } catch (e: IOException) {
                listener.onFailure(it.path, e)
            }
        }
    }

    private fun delete(resource: Resource) {
        val size = resource.readStatus(false).size
        resource.delete()
        deletedByteCount += size
        deletedItemCount++
    }

}
