package l.files.sort;

import android.content.res.Resources;

import java.io.File;
import java.util.List;

interface SortHelper {

  String name(Resources res);

  List<Object> apply(Resources res, File... files);
}
