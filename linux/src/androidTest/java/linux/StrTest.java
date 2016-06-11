package linux;

import junit.framework.TestCase;

import java.lang.reflect.Field;

import static android.test.MoreAsserts.assertMatchesRegex;
import static android.test.MoreAsserts.assertNotEqual;
import static linux.Errno.*;
import static linux.Str.strerror;

public final class StrTest extends TestCase {

    public void test_strerror_returns_string_error() throws Exception {
        assertEquals("EPERM", "Operation not permitted", strerror(EPERM));
        assertEquals("ENOENT", "No such file or directory", strerror(ENOENT));
        assertEquals("ESRCH", "No such process", strerror(ESRCH));
        assertEquals("EINTR", "Interrupted system call", strerror(EINTR));
        assertEquals("EIO", "I/O error", strerror(EIO));
        assertEquals("ENXIO", "No such device or address", strerror(ENXIO));
        assertEquals("E2BIG", "Argument list too long", strerror(E2BIG));
        assertEquals("ENOEXEC", "Exec format error", strerror(ENOEXEC));
        assertMatchesRegex("EBADF", "Bad file number|Bad file descriptor", strerror(EBADF));
        assertEquals("ECHILD", "No child processes", strerror(ECHILD));
        assertEquals("EAGAIN", "Try again", strerror(EAGAIN));
        assertEquals("EWOULDBLOCK", "Try again", strerror(EWOULDBLOCK));
        assertEquals("ENOMEM", "Out of memory", strerror(ENOMEM));
        assertEquals("EACCES", "Permission denied", strerror(EACCES));
        assertEquals("EFAULT", "Bad address", strerror(EFAULT));
        assertEquals("ENOTBLK", "Block device required", strerror(ENOTBLK));
        assertEquals("EBUSY", "Device or resource busy", strerror(EBUSY));
        assertEquals("EEXIST", "File exists", strerror(EEXIST));
        assertEquals("EXDEV", "Cross-device link", strerror(EXDEV));
        assertEquals("ENODEV", "No such device", strerror(ENODEV));
        assertEquals("ENOTDIR", "Not a directory", strerror(ENOTDIR));
        assertEquals("EISDIR", "Is a directory", strerror(EISDIR));
        assertEquals("EINVAL", "Invalid argument", strerror(EINVAL));
        assertEquals("ENFILE", "File table overflow", strerror(ENFILE));
        assertEquals("EMFILE", "Too many open files", strerror(EMFILE));
        assertEquals("ENOTTY", "Not a typewriter", strerror(ENOTTY));
        assertEquals("ETXTBSY", "Text file busy", strerror(ETXTBSY));
        assertEquals("EFBIG", "File too large", strerror(EFBIG));
        assertEquals("ENOSPC", "No space left on device", strerror(ENOSPC));
        assertEquals("ESPIPE", "Illegal seek", strerror(ESPIPE));
        assertEquals("EROFS", "Read-only file system", strerror(EROFS));
        assertEquals("EMLINK", "Too many links", strerror(EMLINK));
        assertEquals("EPIPE", "Broken pipe", strerror(EPIPE));
        assertEquals("EDOM", "Math argument out of domain of func", strerror(EDOM));
        assertEquals("ERANGE", "Math result not representable", strerror(ERANGE));
        assertEquals("EDEADLK", "Resource deadlock would occur", strerror(EDEADLK));
        assertEquals("EDEADLOCK", "Resource deadlock would occur", strerror(EDEADLOCK));
        assertEquals("ENAMETOOLONG", "File name too long", strerror(ENAMETOOLONG));
        assertEquals("ENOLCK", "No record locks available", strerror(ENOLCK));
        assertEquals("ENOSYS", "Function not implemented", strerror(ENOSYS));
        assertEquals("ENOTEMPTY", "Directory not empty", strerror(ENOTEMPTY));
        assertEquals("ELOOP", "Too many symbolic links encountered", strerror(ELOOP));
        assertEquals("ENOMSG", "No message of desired type", strerror(ENOMSG));
        assertEquals("EIDRM", "Identifier removed", strerror(EIDRM));
        assertEquals("ECHRNG", "Channel number out of range", strerror(ECHRNG));
        assertEquals("EL2NSYNC", "Level 2 not synchronized", strerror(EL2NSYNC));
        assertEquals("EL3HLT", "Level 3 halted", strerror(EL3HLT));
        assertEquals("EL3RST", "Level 3 reset", strerror(EL3RST));
        assertEquals("ELNRNG", "Link number out of range", strerror(ELNRNG));
        assertEquals("EUNATCH", "Protocol driver not attached", strerror(EUNATCH));
        assertEquals("ENOCSI", "No CSI structure available", strerror(ENOCSI));
        assertEquals("EL2HLT", "Level 2 halted", strerror(EL2HLT));
        assertEquals("EBADE", "Invalid exchange", strerror(EBADE));
        assertEquals("EBADR", "Invalid request descriptor", strerror(EBADR));
        assertEquals("EXFULL", "Exchange full", strerror(EXFULL));
        assertEquals("ENOANO", "No anode", strerror(ENOANO));
        assertEquals("EBADRQC", "Invalid request code", strerror(EBADRQC));
        assertEquals("EBADSLT", "Invalid slot", strerror(EBADSLT));
        assertEquals("EBFONT", "Bad font file format", strerror(EBFONT));
        assertEquals("ENOSTR", "Device not a stream", strerror(ENOSTR));
        assertEquals("ENODATA", "No data available", strerror(ENODATA));
        assertEquals("ETIME", "Timer expired", strerror(ETIME));
        assertEquals("ENOSR", "Out of streams resources", strerror(ENOSR));
        assertEquals("ENONET", "Machine is not on the network", strerror(ENONET));
        assertEquals("ENOPKG", "Package not installed", strerror(ENOPKG));
        assertEquals("EREMOTE", "Object is remote", strerror(EREMOTE));
        assertEquals("ENOLINK", "Link has been severed", strerror(ENOLINK));
        assertEquals("EADV", "Advertise error", strerror(EADV));
        assertEquals("ESRMNT", "Srmount error", strerror(ESRMNT));
        assertEquals("ECOMM", "Communication error on send", strerror(ECOMM));
        assertEquals("EPROTO", "Protocol error", strerror(EPROTO));
        assertEquals("EMULTIHOP", "Multihop attempted", strerror(EMULTIHOP));
        assertEquals("EDOTDOT", "RFS specific error", strerror(EDOTDOT));
        assertEquals("EBADMSG", "Not a data message", strerror(EBADMSG));
        assertEquals("EOVERFLOW", "Value too large for defined data type", strerror(EOVERFLOW));
        assertEquals("ENOTUNIQ", "Name not unique on network", strerror(ENOTUNIQ));
        assertEquals("EBADFD", "File descriptor in bad state", strerror(EBADFD));
        assertEquals("EREMCHG", "Remote address changed", strerror(EREMCHG));
        assertEquals("ELIBACC", "Can not access a needed shared library", strerror(ELIBACC));
        assertEquals("ELIBBAD", "Accessing a corrupted shared library", strerror(ELIBBAD));
        assertEquals("ELIBSCN", ".lib section in a.out corrupted", strerror(ELIBSCN));
        assertEquals("ELIBMAX", "Attempting to link in too many shared libraries", strerror(ELIBMAX));
        assertEquals("ELIBEXEC", "Cannot exec a shared library directly", strerror(ELIBEXEC));
        assertEquals("EILSEQ", "Illegal byte sequence", strerror(EILSEQ));
        assertEquals("ERESTART", "Interrupted system call should be restarted", strerror(ERESTART));
        assertEquals("ESTRPIPE", "Streams pipe error", strerror(ESTRPIPE));
        assertEquals("EUSERS", "Too many users", strerror(EUSERS));
        assertEquals("ENOTSOCK", "Socket operation on non-socket", strerror(ENOTSOCK));
        assertEquals("EDESTADDRREQ", "Destination address required", strerror(EDESTADDRREQ));
        assertEquals("EMSGSIZE", "Message too long", strerror(EMSGSIZE));
        assertEquals("EPROTOTYPE", "Protocol wrong type for socket", strerror(EPROTOTYPE));
        assertEquals("ENOPROTOOPT", "Protocol not available", strerror(ENOPROTOOPT));
        assertEquals("EPROTONOSUPPORT", "Protocol not supported", strerror(EPROTONOSUPPORT));
        assertEquals("ESOCKTNOSUPPORT", "Socket type not supported", strerror(ESOCKTNOSUPPORT));
        assertEquals("EOPNOTSUPP", "Operation not supported on transport endpoint", strerror(EOPNOTSUPP));
        assertEquals("EPFNOSUPPORT", "Protocol family not supported", strerror(EPFNOSUPPORT));
        assertEquals("EAFNOSUPPORT", "Address family not supported by protocol", strerror(EAFNOSUPPORT));
        assertEquals("EADDRINUSE", "Address already in use", strerror(EADDRINUSE));
        assertEquals("EADDRNOTAVAIL", "Cannot assign requested address", strerror(EADDRNOTAVAIL));
        assertEquals("ENETDOWN", "Network is down", strerror(ENETDOWN));
        assertEquals("ENETUNREACH", "Network is unreachable", strerror(ENETUNREACH));
        assertEquals("ENETRESET", "Network dropped connection because of reset", strerror(ENETRESET));
        assertEquals("ECONNABORTED", "Software caused connection abort", strerror(ECONNABORTED));
        assertEquals("ECONNRESET", "Connection reset by peer", strerror(ECONNRESET));
        assertEquals("ENOBUFS", "No buffer space available", strerror(ENOBUFS));
        assertEquals("EISCONN", "Transport endpoint is already connected", strerror(EISCONN));
        assertEquals("ENOTCONN", "Transport endpoint is not connected", strerror(ENOTCONN));
        assertEquals("ESHUTDOWN", "Cannot send after transport endpoint shutdown", strerror(ESHUTDOWN));
        assertEquals("ETOOMANYREFS", "Too many references: cannot splice", strerror(ETOOMANYREFS));
        assertEquals("ETIMEDOUT", "Connection timed out", strerror(ETIMEDOUT));
        assertEquals("ECONNREFUSED", "Connection refused", strerror(ECONNREFUSED));
        assertEquals("EHOSTDOWN", "Host is down", strerror(EHOSTDOWN));
        assertEquals("EHOSTUNREACH", "No route to host", strerror(EHOSTUNREACH));
        assertEquals("EALREADY", "Operation already in progress", strerror(EALREADY));
        assertEquals("EINPROGRESS", "Operation now in progress", strerror(EINPROGRESS));
        assertEquals("ESTALE", "Stale NFS file handle", strerror(ESTALE));
        assertEquals("EUCLEAN", "Structure needs cleaning", strerror(EUCLEAN));
        assertEquals("ENOTNAM", "Not a XENIX named type file", strerror(ENOTNAM));
        assertEquals("ENAVAIL", "No XENIX semaphores available", strerror(ENAVAIL));
        assertEquals("EISNAM", "Is a named type file", strerror(EISNAM));
        assertEquals("EREMOTEIO", "Remote I/O error", strerror(EREMOTEIO));
        assertEquals("EDQUOT", "Quota exceeded", strerror(EDQUOT));
        assertEquals("ENOMEDIUM", "No medium found", strerror(ENOMEDIUM));
        assertEquals("EMEDIUMTYPE", "Wrong medium type", strerror(EMEDIUMTYPE));
        assertEquals("ECANCELED", "Operation Canceled", strerror(ECANCELED));
        assertEquals("ENOKEY", "Required key not available", strerror(ENOKEY));
        assertEquals("EKEYEXPIRED", "Key has expired", strerror(EKEYEXPIRED));
        assertEquals("EKEYREVOKED", "Key has been revoked", strerror(EKEYREVOKED));
        assertEquals("EKEYREJECTED", "Key was rejected by service", strerror(EKEYREJECTED));
        assertEquals("EOWNERDEAD", "Owner died", strerror(EOWNERDEAD));
        assertEquals("ENOTRECOVERABLE", "State not recoverable", strerror(ENOTRECOVERABLE));
    }

    public void test_strerror_can_handle_all_errno() throws Exception {
        Field[] fields = Errno.class.getFields();
        assertNotEqual(0, fields.length);
        for (Field field : fields) {
            assertNotNull(strerror(field.getInt(null)));
        }
    }

}
