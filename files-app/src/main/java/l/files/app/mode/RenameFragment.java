package l.files.app.mode;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static l.files.app.FilesApp.getBus;
import static l.files.app.Fragments.setArgs;

import android.content.DialogInterface;
import android.os.Bundle;
import com.squareup.otto.Bus;
import java.io.File;
import l.files.R;
import l.files.app.CloseActionModeRequest;
import l.files.app.FileCreationFragment;

public final class RenameFragment extends FileCreationFragment {

  public static final String TAG = RenameFragment.class.getSimpleName();

  private static final String ARG_SOURCE_FILE = "source-file";

  static RenameFragment create(File parent) {
    return setArgs(new RenameFragment(), ARG_SOURCE_FILE, parent.getAbsolutePath());
  }

  Bus bus;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bus = getBus(this);
  }

  protected File getInitialDestinationFile() {
    return getSourceFile();
  }

  private File getSourceFile() {
    return new File(getArguments().getString(ARG_SOURCE_FILE));
  }

  @Override public void onClick(DialogInterface dialog, int which) {
//    if (!getSourceFile().renameTo(getCurrentDestinationFile())) {
//      makeText(getActivity(), R.string.failed_to_rename_file, LENGTH_SHORT).show();
//    }
//    bus.post(CloseActionModeRequest.INSTANCE);
  }
}
