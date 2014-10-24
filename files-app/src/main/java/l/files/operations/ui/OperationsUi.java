package l.files.operations.ui;

import android.content.Context;

import l.files.operations.Clock;
import l.files.operations.Events;

public class OperationsUi {

  public void init(Context context) {
    Events.get().register(new NotificationProvider(context, Clock.system()));
  }

}
