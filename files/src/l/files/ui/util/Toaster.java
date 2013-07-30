package l.files.ui.util;

import android.content.Context;
import android.widget.Toast;

public class Toaster {

  private static final Toaster INSTANCE = new Toaster();

  public static Toaster get() {
    return INSTANCE;
  }

  Toaster() {
  }

  public void toast(Context context, int resId) {
    Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
  }

}
