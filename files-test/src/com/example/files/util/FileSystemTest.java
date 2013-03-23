package com.example.files.util;

import static java.io.File.createTempFile;

import java.io.File;

import junit.framework.TestCase;

public final class FileSystemTest extends TestCase {

    private File mFile;
    private FileSystem mFileSystem;

    public void testHasNoPermissionToReadUnexecutableDirectory() throws Exception {
        assertTrue(mFile.mkdir());
        assertTrue(mFile.setExecutable(false, false));
        assertFalse(mFileSystem.hasPermissionToRead(mFile));
    }

    public void testHasNoPermissionToReadUnreadableFile() throws Exception {
        assertTrue(mFile.createNewFile());
        assertTrue(mFile.setReadable(false, false));
        assertFalse(mFileSystem.hasPermissionToRead(mFile));
    }

    public void testHasNoPermissionToReadUnreadableDirectory() throws Exception {
        assertTrue(mFile.mkdir());
        assertTrue(mFile.setReadable(false, false));
        assertFalse(mFileSystem.hasPermissionToRead(mFile));

    }

    public void testHasPermissionToReadReadableFile() throws Exception {
        assertTrue(mFile.createNewFile());
        assertTrue(mFile.setReadable(true, true));
        assertTrue(mFileSystem.hasPermissionToRead(mFile));
    }

    public void testHasPermissionToReadReadableDirectory() {
        assertTrue(mFile.mkdir());
        assertTrue(mFile.setReadable(true, true));
        assertTrue(mFileSystem.hasPermissionToRead(mFile));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFileSystem = new FileSystem();
        mFile = createTempFile("abc", "def");
        assertTrue(mFile.delete());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        assertTrue(mFile.delete());
    }
}
