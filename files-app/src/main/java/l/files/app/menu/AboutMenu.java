package l.files.app.menu;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.app.OptionsMenuAction;

import static android.content.Intent.ACTION_VIEW;
import static android.view.Menu.CATEGORY_SECONDARY;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;

public final class AboutMenu extends OptionsMenuAction {

  private final Context context;

  public AboutMenu(Context context) {
    super(R.id.about);
    this.context = context;
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(NONE, id(), CATEGORY_SECONDARY, R.string.about)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override protected void onItemSelected(MenuItem item) {
    Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
    context.startActivity(new Intent(ACTION_VIEW, uri));
  }
}
