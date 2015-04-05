package l.files.fs.local

import org.apache.tika.Tika

import l.files.fs.ResourceStatus
import kotlin.properties.Delegates
import com.google.common.net.MediaType
import com.google.common.net.MediaType.OCTET_STREAM
import org.apache.tika.io.TaggedIOException

private object MagicFileTypeDetector : LocalFileTypeDetector() {

    private val tika: Tika by Delegates.lazy { Tika() }

    override fun detectRegularFile(status: ResourceStatus) = try {
        MediaType.parse(tika.detect(LocalPath.check(status.getPath()).file))

    } catch (e: TaggedIOException) {
        if (e.getCause() != null) {
            throw e.getCause()
        } else {
            throw e
        }
    } catch (e: IllegalArgumentException) {
        OCTET_STREAM
    }

}
