package l.files.ui.browser;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

final class HeaderViewHolder extends RecyclerView.ViewHolder {

    static final int LAYOUT_ID = R.layout.files_grid_header;

    private final TextView title;

    HeaderViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(android.R.id.title);
    }

    void bind(Header header) {
        title.setText(header.toString());
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
            ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
        }
    }
}
