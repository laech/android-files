package com.example.files.util.media

import java.io.File
import java.net.URL
import java.util.Map
import java.util.TreeMap
import org.jdom2.input.SAXBuilder

import static com.google.common.base.Charsets.*
import static com.google.common.io.Files.*
import static java.lang.System.*
import static java.util.Locale.*

class MediaGenerator {

  static val MEDIA_URL =
    new URL("http://svn.apache.org/repos/asf/tomcat/trunk/conf/web.xml")

  def static void main(String[] args) {
    val in = MEDIA_URL.openStream
    try {

      val root = new SAXBuilder().build(in).rootElement
      val ns = root.namespace
      val medias = new TreeMap<String, String>
      val mimes = root.getChildren("mime-mapping", ns)
      mimes.forEach[mime |
        medias.put(
          mime.getChildText("extension", ns).toLowerCase(ENGLISH),
          mime.getChildText("mime-type", ns).toLowerCase(ENGLISH)
        )
      ]

      write(generateMediaMap(medias), output("Medias.java"), UTF_8)
      write(generateImageMap(medias), output("Images.java"), UTF_8)

    } finally {
      in.close
    }
  }

  def static output(String name) {
    new File(getProperty("user.home") + "/Desktop/" + name)
  }

  def static generateMediaMap(Map<String, String> types) '''
    // This file is generated by «typeof(MediaGenerator).name»

    package com.example.files.media;

    import static java.util.Locale.ENGLISH;

    class Medias {

      /**
       * Gets the media type for the given file extension.
       *
       * @param extension the file extension without the ".", in any case
       */
      public static String get(String extension) {
        switch (extension.toLowerCase(ENGLISH)) {
          «FOR entry : types.entrySet»
          case "«entry.key»": return "«entry.value»";
          «ENDFOR»
          default: return null;
        }
      }

    }
    '''

  def static generateImageMap(Map<String, String> types) {
    val texts = types.filter(key, value | value.startsWith("text"))
    val images = types.filter(key, value | value.startsWith("image"))
    val audios = types.filter(key, value | value.startsWith("audio"))
    val videos = types.filter(key, value | value.startsWith("video"))
    '''
    // This file is generated by «typeof(MediaGenerator).name»

    package com.example.files.media;

    import static java.util.Locale.ENGLISH;

    import com.example.files.R;

    class Images {

      /**
       * Gets the drawable resource ID for the given file extension.
       *
       * @param extension the file extension without the ".", in any case
       */
      public static int get(String extension) {
        switch (extension.toLowerCase(ENGLISH)) {
          «generateCases(audios, "R.drawable.ic_image")»
          «generateCases(videos, "R.drawable.ic_image")»
          «generateCases(images, "R.drawable.ic_image")»
          «generateCases(texts, "R.drawable.ic_image")»
          default:
            return R.drawable.ic_file;
        }
      }
    }
    '''
  }

  def static generateCases(Map<String, String> map, String result) '''
    «FOR entry : map.entrySet»
    case "«entry.key»":
    «ENDFOR»
      return «result»;
  '''

  def static capacity(int expectedSize) {
    return expectedSize + expectedSize / 3;
  }
}
