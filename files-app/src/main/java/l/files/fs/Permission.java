package l.files.fs;

import java.util.EnumSet;
import java.util.Set;

import static java.util.Collections.emptySet;

public enum Permission
{
    OWNER_READ,
    OWNER_WRITE,
    OWNER_EXECUTE,
    GROUP_READ,
    GROUP_WRITE,
    GROUP_EXECUTE,
    OTHERS_READ,
    OTHERS_WRITE,
    OTHERS_EXECUTE;

    public static Set<Permission> all()
    {
        return EnumSet.allOf(Permission.class);
    }

    public static Set<Permission> none()
    {
        return emptySet();
    }

    public static Set<Permission> read()
    {
        return EnumSet.of(OWNER_READ, GROUP_READ, OTHERS_READ);
    }

    public static Set<Permission> write()
    {
        return EnumSet.of(OWNER_WRITE, GROUP_WRITE, OTHERS_WRITE);
    }

    public static Set<Permission> execute()
    {
        return EnumSet.of(OWNER_EXECUTE, GROUP_EXECUTE, OTHERS_EXECUTE);
    }
}
