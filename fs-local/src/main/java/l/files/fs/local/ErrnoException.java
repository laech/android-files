package l.files.fs.local;

import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

import l.files.fs.AlreadyExist;
import l.files.fs.DirectoryNotEmpty;
import l.files.fs.IOExceptionReason;

import static linux.Errno.E2BIG;
import static linux.Errno.EACCES;
import static linux.Errno.EADDRINUSE;
import static linux.Errno.EADDRNOTAVAIL;
import static linux.Errno.EAFNOSUPPORT;
import static linux.Errno.EAGAIN;
import static linux.Errno.EALREADY;
import static linux.Errno.EBADF;
import static linux.Errno.EBADMSG;
import static linux.Errno.EBUSY;
import static linux.Errno.ECANCELED;
import static linux.Errno.ECHILD;
import static linux.Errno.ECONNABORTED;
import static linux.Errno.ECONNREFUSED;
import static linux.Errno.ECONNRESET;
import static linux.Errno.EDEADLK;
import static linux.Errno.EDESTADDRREQ;
import static linux.Errno.EDOM;
import static linux.Errno.EDQUOT;
import static linux.Errno.EEXIST;
import static linux.Errno.EFAULT;
import static linux.Errno.EFBIG;
import static linux.Errno.EHOSTUNREACH;
import static linux.Errno.EIDRM;
import static linux.Errno.EILSEQ;
import static linux.Errno.EINPROGRESS;
import static linux.Errno.EINTR;
import static linux.Errno.EINVAL;
import static linux.Errno.EIO;
import static linux.Errno.EISCONN;
import static linux.Errno.EISDIR;
import static linux.Errno.ELOOP;
import static linux.Errno.EMFILE;
import static linux.Errno.EMLINK;
import static linux.Errno.EMSGSIZE;
import static linux.Errno.EMULTIHOP;
import static linux.Errno.ENAMETOOLONG;
import static linux.Errno.ENETDOWN;
import static linux.Errno.ENETRESET;
import static linux.Errno.ENETUNREACH;
import static linux.Errno.ENFILE;
import static linux.Errno.ENOBUFS;
import static linux.Errno.ENODATA;
import static linux.Errno.ENODEV;
import static linux.Errno.ENOENT;
import static linux.Errno.ENOEXEC;
import static linux.Errno.ENOLCK;
import static linux.Errno.ENOLINK;
import static linux.Errno.ENOMEM;
import static linux.Errno.ENOMSG;
import static linux.Errno.ENONET;
import static linux.Errno.ENOPROTOOPT;
import static linux.Errno.ENOSPC;
import static linux.Errno.ENOSR;
import static linux.Errno.ENOSTR;
import static linux.Errno.ENOSYS;
import static linux.Errno.ENOTCONN;
import static linux.Errno.ENOTDIR;
import static linux.Errno.ENOTEMPTY;
import static linux.Errno.ENOTSOCK;
import static linux.Errno.ENOTTY;
import static linux.Errno.ENXIO;
import static linux.Errno.EOPNOTSUPP;
import static linux.Errno.EOVERFLOW;
import static linux.Errno.EPERM;
import static linux.Errno.EPIPE;
import static linux.Errno.EPROTO;
import static linux.Errno.EPROTONOSUPPORT;
import static linux.Errno.EPROTOTYPE;
import static linux.Errno.ERANGE;
import static linux.Errno.EROFS;
import static linux.Errno.ESPIPE;
import static linux.Errno.ESRCH;
import static linux.Errno.ESTALE;
import static linux.Errno.ETIME;
import static linux.Errno.ETIMEDOUT;
import static linux.Errno.ETXTBSY;
import static linux.Errno.EXDEV;

final class ErrnoException extends Exception
        implements IOExceptionReason {

    static {
        Native.load();
    }

    public final int errno;

    ErrnoException(int errno) {
        super(errnoName(errno));
        this.errno = errno;
    }

    IOException toIOException(Object... paths) {
        String message = TextUtils.join(", ", paths);
        if (errno == ENOENT) {
            FileNotFoundException e = new FileNotFoundException(message);
            e.initCause(this);
            return e;
        }
        if (errno == EEXIST) {
            return new AlreadyExist(message, this);
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

    @Override
    public String reason() {
        return strerror(errno);
    }

    private static native String strerror(int errnum);

}
