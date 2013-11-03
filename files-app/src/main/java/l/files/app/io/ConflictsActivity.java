package l.files.app.io;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import l.files.R;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static l.files.common.io.Files.toAbsolutePaths;
import static l.files.common.io.Files.toFiles;

public final class ConflictsActivity extends Activity {

  private static final String EXTRA_SOURCES = "l.files.intent.extra.SOURCES";
  private static final String EXTRA_DEST_DIR = "l.files.intent.extra.DEST_DIR";

  public static void start(Context context, Set<File> sources, File destDir) {
    context.startActivity(new Intent(context, ConflictsActivity.class)
        .putExtra(EXTRA_DEST_DIR, destDir.getAbsolutePath())
        .putExtra(EXTRA_SOURCES, toAbsolutePaths(sources)));
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    File destDir = new File(intent.getStringExtra(EXTRA_DEST_DIR));
    File[] sources = toFiles(intent.getStringArrayExtra(EXTRA_SOURCES));
    List<File> conflicts = findConflicts(destDir, sources);
    if (conflicts.isEmpty()) {
      finish();
      return;
    }

    setContentView(R.layout.conflicts_activity);

    int count = conflicts.size();
    updateReplaceAction(count);
    updateSkipAction(count);
    updateSummary(destDir, conflicts);
  }

  private List<File> findConflicts(File destDir, File[] sources) {
    List<File> conflicts = newArrayListWithCapacity(sources.length);
    for (File source : sources) {
      if (new File(destDir, source.getName()).exists())
        conflicts.add(source);
    }
    return conflicts;
  }

  private void updateReplaceAction(int count) {
    ((TextView) findViewById(R.id.replace)).setText(
        getResources().getQuantityString(R.plurals.replace_dest_files, count));
  }

  private void updateSkipAction(int count) {
    ((TextView) findViewById(R.id.skip)).setText(
        getResources().getQuantityString(R.plurals.skip_files, count));
  }

  private void updateSummary(File destDir, List<File> conflicts) {
    TextView summary = (TextView) findViewById(R.id.summary);
    summary.setText(Html.fromHtml(getResources().getQuantityString(
        R.plurals.conflicts_summary_html,
        conflicts.size(),
        destDir.getName(),
        conflicts.get(0).getName(),
        NumberFormat.getInstance().format(conflicts.size()))));
  }
}
