package l.files.fs.local;

class Native {

    static {
        load();
    }

    synchronized static void load() {
        System.loadLibrary("fslocal");
    }

}
