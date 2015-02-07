package l.files.fs.local

import kotlin.platform.platformStatic

/**
 * http://pubs.opengroup.org/onlinepubs/7908799/xsh/dirent.h.html
 */
private data class Dirent(val inode: Long, val type: Int, val name: String) : Native() {

    public class object {

        val DT_UNKNOWN: Int = 0
        val DT_FIFO: Int = 1
        val DT_CHR: Int = 2
        val DT_DIR: Int = 4
        val DT_BLK: Int = 6
        val DT_REG: Int = 8
        val DT_LNK: Int = 10
        val DT_SOCK: Int = 12
        val DT_WHT: Int = 14

        {
            init()
        }

        platformStatic native fun init()

        /**
         * http://pubs.opengroup.org/onlinepubs/7908799/xsh/opendir.html
         */
        throws(javaClass<ErrnoException>())
        platformStatic native fun opendir(dir: String): Long

        /**
         * http://pubs.opengroup.org/onlinepubs/7908799/xsh/closedir.html
         */
        throws(javaClass<ErrnoException>())
        platformStatic native fun closedir(dir: Long)

        /**
         * Note: this will also return the "." and  ".." directories.
         *
         * http://pubs.opengroup.org/onlinepubs/7908799/xsh/readdir.html
         */
        throws(javaClass<ErrnoException>())
        platformStatic native fun readdir(dir: Long): Dirent

    }

}
