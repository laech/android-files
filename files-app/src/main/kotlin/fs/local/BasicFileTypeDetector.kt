package l.files.fs.local

import android.webkit.MimeTypeMap

import com.google.common.net.MediaType

import l.files.fs.ResourceStatus

import com.google.common.net.MediaType.OCTET_STREAM
import org.apache.commons.io.FilenameUtils.getExtension
import l.files.fs.Path

private object BasicFileTypeDetector : LocalFileTypeDetector() {

    override fun detectRegularFile(status: ResourceStatus): MediaType {
        val typeMap = MimeTypeMap.getSingleton()
        val ext = getExtension(status.name)
        val type = typeMap.getMimeTypeFromExtension(ext)
        if (type == null) {
            return OCTET_STREAM
        }
        try {
            return MediaType.parse(type)
        } catch (e: IllegalArgumentException) {
            return OCTET_STREAM
        }
    }

}
