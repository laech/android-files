package linux;

import l.files.fs.Native;

public final class Errno extends Native {

    private Errno() {
    }

    static int placeholder() {
        return -1;
    }

    public static final int EPERM = placeholder();
    public static final int ENOENT = placeholder();
    public static final int ESRCH = placeholder();
    public static final int EINTR = placeholder();
    public static final int EIO = placeholder();
    public static final int ENXIO = placeholder();
    public static final int E2BIG = placeholder();
    public static final int ENOEXEC = placeholder();
    public static final int EBADF = placeholder();
    public static final int ECHILD = placeholder();
    public static final int EAGAIN = placeholder();
    public static final int ENOMEM = placeholder();
    public static final int EACCES = placeholder();
    public static final int EFAULT = placeholder();
    public static final int ENOTBLK = placeholder();
    public static final int EBUSY = placeholder();
    public static final int EEXIST = placeholder();
    public static final int EXDEV = placeholder();
    public static final int ENODEV = placeholder();
    public static final int ENOTDIR = placeholder();
    public static final int EISDIR = placeholder();
    public static final int EINVAL = placeholder();
    public static final int ENFILE = placeholder();
    public static final int EMFILE = placeholder();
    public static final int ENOTTY = placeholder();
    public static final int ETXTBSY = placeholder();
    public static final int EFBIG = placeholder();
    public static final int ENOSPC = placeholder();
    public static final int ESPIPE = placeholder();
    public static final int EROFS = placeholder();
    public static final int EMLINK = placeholder();
    public static final int EPIPE = placeholder();
    public static final int EDOM = placeholder();
    public static final int ERANGE = placeholder();
    public static final int EDEADLK = placeholder();
    public static final int ENAMETOOLONG = placeholder();
    public static final int ENOLCK = placeholder();
    public static final int ENOSYS = placeholder();
    public static final int ENOTEMPTY = placeholder();
    public static final int ELOOP = placeholder();
    public static final int EWOULDBLOCK = placeholder();
    public static final int ENOMSG = placeholder();
    public static final int EIDRM = placeholder();
    public static final int ECHRNG = placeholder();
    public static final int EL2NSYNC = placeholder();
    public static final int EL3HLT = placeholder();
    public static final int EL3RST = placeholder();
    public static final int ELNRNG = placeholder();
    public static final int EUNATCH = placeholder();
    public static final int ENOCSI = placeholder();
    public static final int EL2HLT = placeholder();
    public static final int EBADE = placeholder();
    public static final int EBADR = placeholder();
    public static final int EXFULL = placeholder();
    public static final int ENOANO = placeholder();
    public static final int EBADRQC = placeholder();
    public static final int EBADSLT = placeholder();
    public static final int EDEADLOCK = placeholder();
    public static final int EBFONT = placeholder();
    public static final int ENOSTR = placeholder();
    public static final int ENODATA = placeholder();
    public static final int ETIME = placeholder();
    public static final int ENOSR = placeholder();
    public static final int ENONET = placeholder();
    public static final int ENOPKG = placeholder();
    public static final int EREMOTE = placeholder();
    public static final int ENOLINK = placeholder();
    public static final int EADV = placeholder();
    public static final int ESRMNT = placeholder();
    public static final int ECOMM = placeholder();
    public static final int EPROTO = placeholder();
    public static final int EMULTIHOP = placeholder();
    public static final int EDOTDOT = placeholder();
    public static final int EBADMSG = placeholder();
    public static final int EOVERFLOW = placeholder();
    public static final int ENOTUNIQ = placeholder();
    public static final int EBADFD = placeholder();
    public static final int EREMCHG = placeholder();
    public static final int ELIBACC = placeholder();
    public static final int ELIBBAD = placeholder();
    public static final int ELIBSCN = placeholder();
    public static final int ELIBMAX = placeholder();
    public static final int ELIBEXEC = placeholder();
    public static final int EILSEQ = placeholder();
    public static final int ERESTART = placeholder();
    public static final int ESTRPIPE = placeholder();
    public static final int EUSERS = placeholder();
    public static final int ENOTSOCK = placeholder();
    public static final int EDESTADDRREQ = placeholder();
    public static final int EMSGSIZE = placeholder();
    public static final int EPROTOTYPE = placeholder();
    public static final int ENOPROTOOPT = placeholder();
    public static final int EPROTONOSUPPORT = placeholder();
    public static final int ESOCKTNOSUPPORT = placeholder();
    public static final int EOPNOTSUPP = placeholder();
    public static final int EPFNOSUPPORT = placeholder();
    public static final int EAFNOSUPPORT = placeholder();
    public static final int EADDRINUSE = placeholder();
    public static final int EADDRNOTAVAIL = placeholder();
    public static final int ENETDOWN = placeholder();
    public static final int ENETUNREACH = placeholder();
    public static final int ENETRESET = placeholder();
    public static final int ECONNABORTED = placeholder();
    public static final int ECONNRESET = placeholder();
    public static final int ENOBUFS = placeholder();
    public static final int EISCONN = placeholder();
    public static final int ENOTCONN = placeholder();
    public static final int ESHUTDOWN = placeholder();
    public static final int ETOOMANYREFS = placeholder();
    public static final int ETIMEDOUT = placeholder();
    public static final int ECONNREFUSED = placeholder();
    public static final int EHOSTDOWN = placeholder();
    public static final int EHOSTUNREACH = placeholder();
    public static final int EALREADY = placeholder();
    public static final int EINPROGRESS = placeholder();
    public static final int ESTALE = placeholder();
    public static final int EUCLEAN = placeholder();
    public static final int ENOTNAM = placeholder();
    public static final int ENAVAIL = placeholder();
    public static final int EISNAM = placeholder();
    public static final int EREMOTEIO = placeholder();
    public static final int EDQUOT = placeholder();
    public static final int ENOMEDIUM = placeholder();
    public static final int EMEDIUMTYPE = placeholder();
    public static final int ECANCELED = placeholder();
    public static final int ENOKEY = placeholder();
    public static final int EKEYEXPIRED = placeholder();
    public static final int EKEYREVOKED = placeholder();
    public static final int EKEYREJECTED = placeholder();
    public static final int EOWNERDEAD = placeholder();
    public static final int ENOTRECOVERABLE = placeholder();

    static {
        init();
    }

    private static native void init();
}
