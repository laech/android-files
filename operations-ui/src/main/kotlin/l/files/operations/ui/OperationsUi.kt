package l.files.operations.ui

import android.content.Context
import l.files.operations.ui.notification.NotificationReceiver
import l.files.operations.Events

public class OperationsUi {

  public fun init(context: Context) {
    Events.get()!!.register(NotificationReceiver(context))
  }

}
