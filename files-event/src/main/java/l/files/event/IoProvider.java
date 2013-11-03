package l.files.event;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.event.internal.FileService.*;

import android.app.Application;
import com.squareup.otto.Subscribe;
import java.io.File;

final class IoProvider {

  private final Application context;

  IoProvider(Application context) {
    this.context = checkNotNull(context, "context");
  }

  @Subscribe public void handle(DeleteRequest request) {
    context.startService(delete(context, request.value()));
  }

  @Subscribe public void handle(PasteRequest.Cut request) { // TODO
    for (File file : request.files())
      context.startService(cut(context, file, request.destination()));
  }

  @Subscribe public void handle(PasteRequest.Copy request) { // TODO
    for (File file : request.files())
      context.startService(copy(context, file, request.destination()));
  }
}
