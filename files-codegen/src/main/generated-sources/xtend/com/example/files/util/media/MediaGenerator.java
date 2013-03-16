package com.example.files.util.media;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.MapExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

@SuppressWarnings("all")
public class MediaGenerator {
  private final static URL MEDIA_URL = new Function0<URL>() {
    public URL apply() {
      try {
        URL _uRL = new URL("http://svn.apache.org/repos/asf/tomcat/trunk/conf/web.xml");
        return _uRL;
      } catch (Exception _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    }
  }.apply();
  
  public static void main(final String[] args) {
    try {
      final InputStream in = MediaGenerator.MEDIA_URL.openStream();
      try {
        SAXBuilder _sAXBuilder = new SAXBuilder();
        Document _build = _sAXBuilder.build(in);
        final Element root = _build.getRootElement();
        final Namespace ns = root.getNamespace();
        TreeMap<String,String> _treeMap = new TreeMap<String,String>();
        final TreeMap<String,String> medias = _treeMap;
        final List<Element> mimes = root.getChildren("mime-mapping", ns);
        final Procedure1<Element> _function = new Procedure1<Element>() {
            public void apply(final Element mime) {
              String _childText = mime.getChildText("extension", ns);
              String _lowerCase = _childText.toLowerCase(Locale.ENGLISH);
              String _childText_1 = mime.getChildText("mime-type", ns);
              String _lowerCase_1 = _childText_1.toLowerCase(Locale.ENGLISH);
              medias.put(_lowerCase, _lowerCase_1);
            }
          };
        IterableExtensions.<Element>forEach(mimes, _function);
        CharSequence _generateMedias = MediaGenerator.generateMedias(medias);
        File _output = MediaGenerator.output("Medias.java");
        Files.write(_generateMedias, _output, Charsets.UTF_8);
        CharSequence _generateImages = MediaGenerator.generateImages(medias);
        File _output_1 = MediaGenerator.output("Images.java");
        Files.write(_generateImages, _output_1, Charsets.UTF_8);
      } finally {
        in.close();
      }
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public static File output(final String name) {
    String _property = System.getProperty("user.home");
    String _plus = (_property + "/Desktop/");
    String _plus_1 = (_plus + name);
    File _file = new File(_plus_1);
    return _file;
  }
  
  public static CharSequence generateMedias(final Map<String,String> types) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("// This file is generated by ");
    String _name = MediaGenerator.class.getName();
    _builder.append(_name, "");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("package com.example.files.media;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import static java.util.Collections.unmodifiableMap;");
    _builder.newLine();
    _builder.append("import static java.util.Locale.ENGLISH;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import java.util.HashMap;");
    _builder.newLine();
    _builder.append("import java.util.Map;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Medias {");
    _builder.newLine();
    _builder.newLine();
    _builder.append("  ");
    _builder.append("private static final Map<String, String> TYPES = mediaTypes();");
    _builder.newLine();
    _builder.newLine();
    _builder.append("  ");
    _builder.append("private static Map<String, String> mediaTypes() {");
    _builder.newLine();
    _builder.append("    ");
    CharSequence _map = MediaGenerator.map(types);
    _builder.append(_map, "    ");
    _builder.newLineIfNotEmpty();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("  ");
    _builder.append("/**");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("* Gets the media type for the given file extension.");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("*");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("* @param extension the file extension without the \".\", case insensitive");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("public static String get(String extension) {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return TYPES.get(extension.toLowerCase(ENGLISH));");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    return _builder;
  }
  
  public static CharSequence map(final Map<String,String> map) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Map<String, String> map = new HashMap<String, String>(");
    int _size = map.size();
    int _capacity = MediaGenerator.capacity(_size);
    _builder.append(_capacity, "");
    _builder.append(");");
    _builder.newLineIfNotEmpty();
    {
      Set<Entry<String,String>> _entrySet = map.entrySet();
      for(final Entry<String,String> entry : _entrySet) {
        _builder.append("map.put(\"");
        String _key = entry.getKey();
        _builder.append(_key, "");
        _builder.append("\", \"");
        String _value = entry.getValue();
        _builder.append(_value, "");
        _builder.append("\");");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("return unmodifiableMap(map);");
    _builder.newLine();
    return _builder;
  }
  
  public static CharSequence generateImages(final Map<String,String> types) {
    CharSequence _xblockexpression = null;
    {
      final Function2<String,String,Boolean> _function = new Function2<String,String,Boolean>() {
          public Boolean apply(final String key, final String value) {
            boolean _startsWith = value.startsWith("text");
            return Boolean.valueOf(_startsWith);
          }
        };
      Map<String,String> _filter = MapExtensions.<String, String>filter(types, _function);
      final Set<String> texts = _filter.keySet();
      final Function2<String,String,Boolean> _function_1 = new Function2<String,String,Boolean>() {
          public Boolean apply(final String key, final String value) {
            boolean _startsWith = value.startsWith("image");
            return Boolean.valueOf(_startsWith);
          }
        };
      Map<String,String> _filter_1 = MapExtensions.<String, String>filter(types, _function_1);
      final Set<String> images = _filter_1.keySet();
      final Function2<String,String,Boolean> _function_2 = new Function2<String,String,Boolean>() {
          public Boolean apply(final String key, final String value) {
            boolean _startsWith = value.startsWith("audio");
            return Boolean.valueOf(_startsWith);
          }
        };
      Map<String,String> _filter_2 = MapExtensions.<String, String>filter(types, _function_2);
      final Set<String> audios = _filter_2.keySet();
      final Function2<String,String,Boolean> _function_3 = new Function2<String,String,Boolean>() {
          public Boolean apply(final String key, final String value) {
            boolean _startsWith = value.startsWith("video");
            return Boolean.valueOf(_startsWith);
          }
        };
      Map<String,String> _filter_3 = MapExtensions.<String, String>filter(types, _function_3);
      final Set<String> videos = _filter_3.keySet();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("// This file is generated by ");
      String _name = MediaGenerator.class.getName();
      _builder.append(_name, "");
      _builder.newLineIfNotEmpty();
      _builder.newLine();
      _builder.append("package com.example.files.media;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("import static java.util.Collections.unmodifiableSet;");
      _builder.newLine();
      _builder.append("import static java.util.Locale.ENGLISH;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("import java.util.HashSet;");
      _builder.newLine();
      _builder.append("import java.util.Set;");
      _builder.newLine();
      _builder.append("import com.example.files.R;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("class Images {");
      _builder.newLine();
      _builder.newLine();
      _builder.append("  ");
      _builder.append("private static final Set<String> AUDIOS = audios();");
      _builder.newLine();
      _builder.append("  ");
      _builder.append("private static final Set<String> VIDEOS = videos();");
      _builder.newLine();
      _builder.append("  ");
      _builder.append("private static final Set<String> IMAGES = images();");
      _builder.newLine();
      _builder.append("  ");
      _builder.append("private static final Set<String> TEXTS = texts();");
      _builder.newLine();
      _builder.newLine();
      _builder.append("  ");
      _builder.append("private static Set<String> audios() {");
      _builder.newLine();
      _builder.append("    ");
      CharSequence _set = MediaGenerator.set(audios);
      _builder.append(_set, "    ");
      _builder.newLineIfNotEmpty();
      _builder.append("  ");
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("  ");
      _builder.append("private static Set<String> videos() {");
      _builder.newLine();
      _builder.append("    ");
      CharSequence _set_1 = MediaGenerator.set(videos);
      _builder.append(_set_1, "    ");
      _builder.newLineIfNotEmpty();
      _builder.append("  ");
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("  ");
      _builder.append("private static Set<String> images() {");
      _builder.newLine();
      _builder.append("    ");
      CharSequence _set_2 = MediaGenerator.set(images);
      _builder.append(_set_2, "    ");
      _builder.newLineIfNotEmpty();
      _builder.append("  ");
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("  ");
      _builder.append("private static Set<String> texts() {");
      _builder.newLine();
      _builder.append("    ");
      CharSequence _set_3 = MediaGenerator.set(texts);
      _builder.append(_set_3, "    ");
      _builder.newLineIfNotEmpty();
      _builder.append("  ");
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("  ");
      _builder.append("public static int get(String extension) {");
      _builder.newLine();
      _builder.append("    ");
      _builder.append("String ext = extension.toLowerCase(ENGLISH);");
      _builder.newLine();
      _builder.append("    ");
      _builder.append("if (ext.equals(\"pdf\")) return R.drawable.ic_file_pdf;");
      _builder.newLine();
      _builder.append("    ");
      _builder.append("if (AUDIOS.contains(ext)) return R.drawable.ic_image;");
      _builder.newLine();
      _builder.append("    ");
      _builder.append("if (VIDEOS.contains(ext)) return R.drawable.ic_image;");
      _builder.newLine();
      _builder.append("    ");
      _builder.append("if (IMAGES.contains(ext)) return R.drawable.ic_image;");
      _builder.newLine();
      _builder.append("    ");
      _builder.append("if (TEXTS.contains(ext)) return R.drawable.ic_image;");
      _builder.newLine();
      _builder.append("    ");
      _builder.append("if (AUDIOS.contains(ext)) return R.drawable.ic_image;");
      _builder.newLine();
      _builder.append("    ");
      _builder.append("return R.drawable.ic_file;");
      _builder.newLine();
      _builder.append("  ");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _xblockexpression = (_builder);
    }
    return _xblockexpression;
  }
  
  public static CharSequence set(final Set<String> set) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Set<String> set = new HashSet<String>(");
    int _size = set.size();
    int _capacity = MediaGenerator.capacity(_size);
    _builder.append(_capacity, "");
    _builder.append(");");
    _builder.newLineIfNotEmpty();
    {
      for(final String element : set) {
        _builder.append("set.add(\"");
        _builder.append(element, "");
        _builder.append("\");");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("return unmodifiableSet(set);");
    _builder.newLine();
    return _builder;
  }
  
  public static int capacity(final int expectedSize) {
    int _divide = (expectedSize / 3);
    return (expectedSize + _divide);
  }
}
