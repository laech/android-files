package l.files.fs.local

import kotlin.platform.platformStatic

/**
 * http://pubs.opengroup.org/onlinepubs/7908799/xsh/pwd.h.html
 */
private data class Passwd(val name: String,
                  val uid: Int,
                  val gid: Int,
                  val dir: String,
                  val shell: String) : Native() {

    public class object {

        {
            init()
        }

        platformStatic native private fun init()

        /**
         * http://pubs.opengroup.org/onlinepubs/7908799/xsh/getpwuid.html
         */
        throws(javaClass<ErrnoException>())
        platformStatic native fun getpwuid(uid: Int): Passwd

    }

}
