package l.files.app.menu;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static l.files.app.Fragments.setArgs;
import static l.files.common.io.Files.getNonExistentDestinationFile;

import android.content.DialogInterface;
import java.io.File;
import l.files.R;
import l.files.app.FileCreationFragment;

public final class NewDirFragment extends FileCreationFragment {

  public static final String TAG = NewDirFragment.class.getSimpleName();

  private static final String ARG_PARENT_DIR = "parent";

  static NewDirFragment create(File parent) {
    return setArgs(new NewDirFragment(), ARG_PARENT_DIR, parent.getAbsolutePath());
  }

  @Override protected File getInitialDestinationFile() {
    File parent = new File(getArguments().getString(ARG_PARENT_DIR));
    return getNonExistentDestinationFile(
        new File(parent, getString(R.string.untitled_dir)), parent);
  }

  @Override public void onClick(DialogInterface dialog, int which) {
    if (!getCurrentDestinationFile().mkdir()) {
      makeText(getActivity(), getString(R.string.mkdir_failed), LENGTH_SHORT).show();
    }
  }
}
