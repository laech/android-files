package com.example.files.util.media

import java.io.File
import java.net.URL
import java.util.Map
import java.util.Set
import java.util.TreeMap
import org.jdom2.input.SAXBuilder

import static com.example.files.util.media.MediaGenerator.*
import static com.google.common.base.Charsets.*
import static com.google.common.io.Files.*
import static java.util.Locale.*

class MediaGenerator {

  static val MEDIA_URL =
    new URL("http://svn.apache.org/repos/asf/tomcat/trunk/conf/web.xml")

  def static void generate(File androidManifest, File outputDirectory) {
    val pkg = extractPackage(androidManifest) + ".media"
    val root = new SAXBuilder().build(MEDIA_URL).rootElement
    val ns = root.namespace
    val medias = new TreeMap<String, String>
    val mimes = root.getChildren("mime-mapping", ns)
    mimes.forEach[mime |
      medias.put(
        mime.getChildText("extension", ns).toLowerCase(ENGLISH),
        mime.getChildText("mime-type", ns).toLowerCase(ENGLISH)
      )
    ]

    val dir = new File(outputDirectory, pkg.replaceAll("\\.", File::separator))
    if (!(dir.mkdirs || dir.directory)) {
      throw new AssertionError("Failed to create directory: " + outputDirectory)
    }

    write(generateMedias(medias, pkg), new File(dir, "Medias.java"), UTF_8)
    write(generateImages(medias, pkg), new File(dir, "Images.java"), UTF_8)
  }

  def private static extractPackage(File androidManifest) {
    new SAXBuilder().build(androidManifest).rootElement.getAttributeValue("package")
  }

  def private static generateMedias(Map<String, String> types, String pkg) '''
    package «pkg»;

    import static java.util.Collections.unmodifiableMap;
    import static java.util.Locale.ENGLISH;

    import java.util.HashMap;
    import java.util.Map;

    class Medias {

      private static final Map<String, String> TYPES = mediaTypes();

      private static Map<String, String> mediaTypes() {
        «map(types)»
      }

      /**
       * Gets the media type for the given file extension.
       *
       * @param extension the file extension without the ".", case insensitive
       */
      public static String get(String extension) {
        return TYPES.get(extension.toLowerCase(ENGLISH));
      }

    }
  '''

  def private static map(Map<String, String> map) '''
    Map<String, String> map = new HashMap<String, String>(«capacity(map.size)»);
    «FOR entry : map.entrySet»
    map.put("«entry.key»", "«entry.value»");
    «ENDFOR»
    return unmodifiableMap(map);
  '''

  def private static generateImages(Map<String, String> types, String pkg) {
    val texts = types.filter(key, value | value.startsWith("text")).keySet
    val images = types.filter(key, value | value.startsWith("image")).keySet
    val audios = types.filter(key, value | value.startsWith("audio")).keySet
    val videos = types.filter(key, value | value.startsWith("video")).keySet
    '''
    package «pkg»;

    import static java.util.Collections.unmodifiableSet;
    import static java.util.Locale.ENGLISH;

    import java.util.HashSet;
    import java.util.Set;
    import com.example.files.R;

    class Images {

      private static final Set<String> AUDIOS = audios();
      private static final Set<String> VIDEOS = videos();
      private static final Set<String> IMAGES = images();
      private static final Set<String> TEXTS = texts();

      private static Set<String> audios() {
        «set(audios)»
      }

      private static Set<String> videos() {
        «set(videos)»
      }

      private static Set<String> images() {
        «set(images)»
      }

      private static Set<String> texts() {
        «set(texts)»
      }

      public static int get(String extension) {
        String ext = extension.toLowerCase(ENGLISH);
        if (ext.equals("pdf")) return R.drawable.ic_file_pdf;
        if (AUDIOS.contains(ext)) return R.drawable.ic_image;
        if (VIDEOS.contains(ext)) return R.drawable.ic_image;
        if (IMAGES.contains(ext)) return R.drawable.ic_image;
        if (TEXTS.contains(ext)) return R.drawable.ic_image;
        if (AUDIOS.contains(ext)) return R.drawable.ic_image;
        return R.drawable.ic_file;
      }
    }
    '''
  }

  def private static set(Set<String> set) '''
    Set<String> set = new HashSet<String>(«capacity(set.size)»);
    «FOR element : set»
    set.add("«element»");
    «ENDFOR»
    return unmodifiableSet(set);
  '''

  def private static capacity(int expectedSize) {
    return expectedSize + expectedSize / 3;
  }
}
