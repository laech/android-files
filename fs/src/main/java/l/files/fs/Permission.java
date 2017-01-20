package l.files.fs;

import java.util.EnumSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static l.files.fs.Stat.S_IRGRP;
import static l.files.fs.Stat.S_IROTH;
import static l.files.fs.Stat.S_IRUSR;
import static l.files.fs.Stat.S_IWGRP;
import static l.files.fs.Stat.S_IWOTH;
import static l.files.fs.Stat.S_IWUSR;
import static l.files.fs.Stat.S_IXGRP;
import static l.files.fs.Stat.S_IXOTH;
import static l.files.fs.Stat.S_IXUSR;

public enum Permission {

    OWNER_READ(S_IRUSR),
    OWNER_WRITE(S_IWUSR),
    OWNER_EXECUTE(S_IXUSR),
    GROUP_READ(S_IRGRP),
    GROUP_WRITE(S_IWGRP),
    GROUP_EXECUTE(S_IXGRP),
    OTHERS_READ(S_IROTH),
    OTHERS_WRITE(S_IWOTH),
    OTHERS_EXECUTE(S_IXOTH);

    private final int bit;

    Permission(int bit) {
        this.bit = bit;
    }

    static Set<Permission> fromStatMode(int mode) {
        Set<Permission> permissions = EnumSet.noneOf(Permission.class);
        for (Permission permission : values()) {
            if ((mode & permission.bit) != 0) {
                permissions.add(permission);
            }
        }
        return unmodifiableSet(permissions);
    }

    static int toStatMode(Iterable<Permission> permissions) {
        int mode = 0;
        for (Permission permission : permissions) {
            mode |= permission.bit;
        }
        return mode;
    }

    public static Set<Permission> all() {
        return EnumSet.allOf(Permission.class);
    }

    public static Set<Permission> none() {
        return emptySet();
    }

    public static Set<Permission> read() {
        return EnumSet.of(OWNER_READ, GROUP_READ, OTHERS_READ);
    }

    public static Set<Permission> write() {
        return EnumSet.of(OWNER_WRITE, GROUP_WRITE, OTHERS_WRITE);
    }

    public static Set<Permission> execute() {
        return EnumSet.of(OWNER_EXECUTE, GROUP_EXECUTE, OTHERS_EXECUTE);
    }
}
