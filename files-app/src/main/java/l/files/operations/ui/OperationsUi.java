package l.files.operations.ui;

import android.content.Context;

import l.files.operations.Clock;
import l.files.operations.OperationService;

public final class OperationsUi {

  public void init(Context context) {
    OperationService.addListener(
        new NotificationProvider(context, Clock.system()));
  }

}
