package l.files.fs.local

import kotlin.platform.platformStatic

/**
 * http://pubs.opengroup.org/onlinepubs/7908799/xsh/grp.h.html
 */
private data class Group(val name: String, val gid: Int) : Native() {

    public companion object {

        init {
            init()
        }

        platformStatic native private fun init()

        /**
         * http://pubs.opengroup.org/onlinepubs/7908799/xsh/getgrgid.html
         */
        throws(javaClass<ErrnoException>())
        platformStatic native fun getgrgid(gid: Int): Group

    }

}
