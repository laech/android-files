package l.files.fs

import com.google.common.net.MediaType

import java.io.IOException

trait FileTypeDetector {

    /**
     * Detects the content type of a file, if the file is a link returns the
     * content type of the target file.
     */
    throws(javaClass<IOException>())
    fun detect(path: Path): MediaType

    /**
     * Detects the content type of a file, use an existing status as hint.
     */
    throws(javaClass<IOException>())
    fun detect(status: ResourceStatus): MediaType

}
