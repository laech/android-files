package l.files.ui.bookmarks;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import l.files.R;
import l.files.common.view.ActionModeProvider;
import l.files.fs.Resource;
import l.files.ui.FileLabels;
import l.files.ui.Icons;
import l.files.ui.StableAdapter;
import l.files.ui.browser.OnOpenFileListener;
import l.files.ui.selection.Selection;
import l.files.ui.selection.SelectionModeViewHolder;

import static java.util.Objects.requireNonNull;
import static l.files.common.view.Views.find;
import static l.files.ui.Icons.directoryIconStringId;

final class BookmarksAdapter extends StableAdapter<Resource, BookmarksAdapter.Holder> {

  private final ActionModeProvider actionModeProvider;
  private final ActionMode.Callback actionModeCallback;
  private final Selection<Resource> selection;
  private final OnOpenFileListener listener;

  BookmarksAdapter(
      Selection<Resource> selection,
      ActionModeProvider actionModeProvider,
      ActionMode.Callback actionModeCallback,
      OnOpenFileListener listener) {
    this.listener = requireNonNull(listener, "listener");
    this.selection = requireNonNull(selection, "selection");
    this.actionModeProvider = requireNonNull(actionModeProvider, "actionModeProvider");
    this.actionModeCallback = requireNonNull(actionModeCallback, "actionModeCallback");
  }

  @Override public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    View view = inflater.inflate(R.layout.bookmark_item, parent, false);
    return new Holder(view);
  }

  @Override public void onBindViewHolder(Holder holder, int position) {
    holder.bind(getItem(position));
  }

  @Override public Resource getItemIdObject(int position) {
    return getItem(position);
  }

  class Holder extends SelectionModeViewHolder<Resource, Resource> {
    TextView title;
    TextView icon;

    Holder(View itemView) {
      super(itemView, selection, actionModeProvider, actionModeCallback);
      title = find(android.R.id.title, this);
      icon = find(android.R.id.icon, this);
      icon.setTypeface(Icons.font(itemView.getResources().getAssets()));
    }

    @Override protected Resource itemId(Resource resource) {
      return resource;
    }

    @Override public void bind(Resource resource) {
      super.bind(resource);
      title.setText(FileLabels.get(title.getResources(), resource));
      icon.setText(directoryIconStringId(resource));
    }

    @Override protected void onClick(View v, Resource item) {
      listener.onOpen(item);
    }
  }
}
