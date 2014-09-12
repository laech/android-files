package l.files.operations.ui;

import android.content.Context;

import l.files.operations.Events;

public class OperationsUi {

  public void init(Context context) {
    Events.get().register(new NotificationReceiver(context));
  }

}
