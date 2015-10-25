package l.files.fs.local;

import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

import l.files.fs.DirectoryNotEmpty;

public final class ErrnoException extends Exception {

    public static final int EPERM = 1;
    public static final int ENOENT = 2;
    public static final int ESRCH = 3;
    public static final int EINTR = 4;
    public static final int EIO = 5;
    public static final int ENXIO = 6;
    public static final int E2BIG = 7;
    public static final int ENOEXEC = 8;
    public static final int EBADF = 9;
    public static final int ECHILD = 10;
    public static final int EAGAIN = 11;
    public static final int ENOMEM = 12;
    public static final int EACCES = 13;
    public static final int EFAULT = 14;
    public static final int ENOTBLK = 15;
    public static final int EBUSY = 16;
    public static final int EEXIST = 17;
    public static final int EXDEV = 18;
    public static final int ENODEV = 19;
    public static final int ENOTDIR = 20;
    public static final int EISDIR = 21;
    public static final int EINVAL = 22;
    public static final int ENFILE = 23;
    public static final int EMFILE = 24;
    public static final int ENOTTY = 25;
    public static final int ETXTBSY = 26;
    public static final int EFBIG = 27;
    public static final int ENOSPC = 28;
    public static final int ESPIPE = 29;
    public static final int EROFS = 30;
    public static final int EMLINK = 31;
    public static final int EPIPE = 32;
    public static final int EDOM = 33;
    public static final int ERANGE = 34;
    public static final int EDEADLK = 35;
    public static final int ENAMETOOLONG = 36;
    public static final int ENOLCK = 37;
    public static final int ENOSYS = 38;
    public static final int ENOTEMPTY = 39;
    public static final int ELOOP = 40;
    public static final int EWOULDBLOCK = EAGAIN;
    public static final int ENOMSG = 42;
    public static final int EIDRM = 43;
    public static final int ECHRNG = 44;
    public static final int EL2NSYNC = 45;
    public static final int EL3HLT = 46;
    public static final int EL3RST = 47;
    public static final int ELNRNG = 48;
    public static final int EUNATCH = 49;
    public static final int ENOCSI = 50;
    public static final int EL2HLT = 51;
    public static final int EBADE = 52;
    public static final int EBADR = 53;
    public static final int EXFULL = 54;
    public static final int ENOANO = 55;
    public static final int EBADRQC = 56;
    public static final int EBADSLT = 57;
    public static final int EDEADLOCK = EDEADLK;
    public static final int EBFONT = 59;
    public static final int ENOSTR = 60;
    public static final int ENODATA = 61;
    public static final int ETIME = 62;
    public static final int ENOSR = 63;
    public static final int ENONET = 64;
    public static final int ENOPKG = 65;
    public static final int EREMOTE = 66;
    public static final int ENOLINK = 67;
    public static final int EADV = 68;
    public static final int ESRMNT = 69;
    public static final int ECOMM = 70;
    public static final int EPROTO = 71;
    public static final int EMULTIHOP = 72;
    public static final int EDOTDOT = 73;
    public static final int EBADMSG = 74;
    public static final int EOVERFLOW = 75;
    public static final int ENOTUNIQ = 76;
    public static final int EBADFD = 77;
    public static final int EREMCHG = 78;
    public static final int ELIBACC = 79;
    public static final int ELIBBAD = 80;
    public static final int ELIBSCN = 81;
    public static final int ELIBMAX = 82;
    public static final int ELIBEXEC = 83;
    public static final int EILSEQ = 84;
    public static final int ERESTART = 85;
    public static final int ESTRPIPE = 86;
    public static final int EUSERS = 87;
    public static final int ENOTSOCK = 88;
    public static final int EDESTADDRREQ = 89;
    public static final int EMSGSIZE = 90;
    public static final int EPROTOTYPE = 91;
    public static final int ENOPROTOOPT = 92;
    public static final int EPROTONOSUPPORT = 93;
    public static final int ESOCKTNOSUPPORT = 94;
    public static final int EOPNOTSUPP = 95;
    public static final int EPFNOSUPPORT = 96;
    public static final int EAFNOSUPPORT = 97;
    public static final int EADDRINUSE = 98;
    public static final int EADDRNOTAVAIL = 99;
    public static final int ENETDOWN = 100;
    public static final int ENETUNREACH = 101;
    public static final int ENETRESET = 102;
    public static final int ECONNABORTED = 103;
    public static final int ECONNRESET = 104;
    public static final int ENOBUFS = 105;
    public static final int EISCONN = 106;
    public static final int ENOTCONN = 107;
    public static final int ESHUTDOWN = 108;
    public static final int ETOOMANYREFS = 109;
    public static final int ETIMEDOUT = 110;
    public static final int ECONNREFUSED = 111;
    public static final int EHOSTDOWN = 112;
    public static final int EHOSTUNREACH = 113;
    public static final int EALREADY = 114;
    public static final int EINPROGRESS = 115;
    public static final int ESTALE = 116;
    public static final int EUCLEAN = 117;
    public static final int ENOTNAM = 118;
    public static final int ENAVAIL = 119;
    public static final int EISNAM = 120;
    public static final int EREMOTEIO = 121;
    public static final int EDQUOT = 122;
    public static final int ENOMEDIUM = 123;
    public static final int EMEDIUMTYPE = 124;
    public static final int ECANCELED = 125;
    public static final int ENOKEY = 126;
    public static final int EKEYEXPIRED = 127;
    public static final int EKEYREVOKED = 128;
    public static final int EKEYREJECTED = 129;
    public static final int EOWNERDEAD = 130;
    public static final int ENOTRECOVERABLE = 131;

    static {
        Native.load();
    }

    public final int errno;

    ErrnoException(int errno) {
        super(errnoName(errno));
        this.errno = errno;
    }

    IOException toIOException(String... paths) {
        String message = TextUtils.join(", ", paths);
        if (errno == ENOENT) {
            FileNotFoundException e = new FileNotFoundException(message);
            e.initCause(this);
            return e;
        }
        if (errno == ENOTEMPTY) {
            return new DirectoryNotEmpty(message, this);
        }
        return new IOException(message, this);
    }

    private static String errnoName(int errno) {
        if (errno == E2BIG) return "E2BIG";
        if (errno == EACCES) return "EACCES";
        if (errno == EADDRINUSE) return "EADDRINUSE";
        if (errno == EADDRNOTAVAIL) return "EADDRNOTAVAIL";
        if (errno == EAFNOSUPPORT) return "EAFNOSUPPORT";
        if (errno == EAGAIN) return "EAGAIN";
        if (errno == EALREADY) return "EALREADY";
        if (errno == EBADF) return "EBADF";
        if (errno == EBADMSG) return "EBADMSG";
        if (errno == EBUSY) return "EBUSY";
        if (errno == ECANCELED) return "ECANCELED";
        if (errno == ECHILD) return "ECHILD";
        if (errno == ECONNABORTED) return "ECONNABORTED";
        if (errno == ECONNREFUSED) return "ECONNREFUSED";
        if (errno == ECONNRESET) return "ECONNRESET";
        if (errno == EDEADLK) return "EDEADLK";
        if (errno == EDESTADDRREQ) return "EDESTADDRREQ";
        if (errno == EDOM) return "EDOM";
        if (errno == EDQUOT) return "EDQUOT";
        if (errno == EEXIST) return "EEXIST";
        if (errno == EFAULT) return "EFAULT";
        if (errno == EFBIG) return "EFBIG";
        if (errno == EHOSTUNREACH) return "EHOSTUNREACH";
        if (errno == EIDRM) return "EIDRM";
        if (errno == EILSEQ) return "EILSEQ";
        if (errno == EINPROGRESS) return "EINPROGRESS";
        if (errno == EINTR) return "EINTR";
        if (errno == EINVAL) return "EINVAL";
        if (errno == EIO) return "EIO";
        if (errno == EISCONN) return "EISCONN";
        if (errno == EISDIR) return "EISDIR";
        if (errno == ELOOP) return "ELOOP";
        if (errno == EMFILE) return "EMFILE";
        if (errno == EMLINK) return "EMLINK";
        if (errno == EMSGSIZE) return "EMSGSIZE";
        if (errno == EMULTIHOP) return "EMULTIHOP";
        if (errno == ENAMETOOLONG) return "ENAMETOOLONG";
        if (errno == ENETDOWN) return "ENETDOWN";
        if (errno == ENETRESET) return "ENETRESET";
        if (errno == ENETUNREACH) return "ENETUNREACH";
        if (errno == ENFILE) return "ENFILE";
        if (errno == ENOBUFS) return "ENOBUFS";
        if (errno == ENODATA) return "ENODATA";
        if (errno == ENODEV) return "ENODEV";
        if (errno == ENOENT) return "ENOENT";
        if (errno == ENOEXEC) return "ENOEXEC";
        if (errno == ENOLCK) return "ENOLCK";
        if (errno == ENOLINK) return "ENOLINK";
        if (errno == ENOMEM) return "ENOMEM";
        if (errno == ENOMSG) return "ENOMSG";
        if (errno == ENONET) return "ENONET";
        if (errno == ENOPROTOOPT) return "ENOPROTOOPT";
        if (errno == ENOSPC) return "ENOSPC";
        if (errno == ENOSR) return "ENOSR";
        if (errno == ENOSTR) return "ENOSTR";
        if (errno == ENOSYS) return "ENOSYS";
        if (errno == ENOTCONN) return "ENOTCONN";
        if (errno == ENOTDIR) return "ENOTDIR";
        if (errno == ENOTEMPTY) return "ENOTEMPTY";
        if (errno == ENOTSOCK) return "ENOTSOCK";
        if (errno == ENOTTY) return "ENOTTY";
        if (errno == ENXIO) return "ENXIO";
        if (errno == EOPNOTSUPP) return "EOPNOTSUPP";
        if (errno == EOVERFLOW) return "EOVERFLOW";
        if (errno == EPERM) return "EPERM";
        if (errno == EPIPE) return "EPIPE";
        if (errno == EPROTO) return "EPROTO";
        if (errno == EPROTONOSUPPORT) return "EPROTONOSUPPORT";
        if (errno == EPROTOTYPE) return "EPROTOTYPE";
        if (errno == ERANGE) return "ERANGE";
        if (errno == EROFS) return "EROFS";
        if (errno == ESPIPE) return "ESPIPE";
        if (errno == ESRCH) return "ESRCH";
        if (errno == ESTALE) return "ESTALE";
        if (errno == ETIME) return "ETIME";
        if (errno == ETIMEDOUT) return "ETIMEDOUT";
        if (errno == ETXTBSY) return "ETXTBSY";
        if (errno == EXDEV) return "EXDEV";
        return null;
    }

    public String strerror() {
        return strerror(errno);
    }

    private static native String strerror(int errnum);

}