package com.example.files.media;

import static com.example.files.test.TempFolder.newTempFolder;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;

import junit.framework.TestCase;

import com.example.files.R;
import com.example.files.test.TempFolder;

public final class ImageMapTest extends TestCase {

  private TempFolder folder;
  private ImageMap images;

  public void testGetsImageIconFromExtension() {
    assertEquals(R.drawable.ic_image, images.get(createFile("jpg")));
  }

  public void testGetsImageIconFromExtensionIgnoringCase() {
    assertEquals(R.drawable.ic_image, images.get(createFile("jPg")));
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    images = new ImageMap();
    folder = newTempFolder();
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    folder.delete();
  }

  private File createFile(String ext) {
    File file = mock(File.class);
    given(file.getName()).willReturn("a." + ext);
    return file;
  }
}
