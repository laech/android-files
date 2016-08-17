package l.files.ui.browser;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static l.files.ui.base.view.Views.find;

final class HeaderViewHolder extends RecyclerView.ViewHolder {

    private final TextView title;

    HeaderViewHolder(View itemView) {
        super(itemView);
        title = find(android.R.id.title, this);
    }

    void bind(Header header) {
        title.setText(header.toString());
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
            ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
        }
    }
}
