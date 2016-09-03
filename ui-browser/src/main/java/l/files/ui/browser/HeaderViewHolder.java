package l.files.ui.browser;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.ViewGroup;

import l.files.ui.browser.databinding.FilesGridHeaderBinding;

final class HeaderViewHolder extends RecyclerView.ViewHolder {

    private final FilesGridHeaderBinding binding;

    HeaderViewHolder(FilesGridHeaderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    void bind(Header header) {
        binding.setHeader(header);
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
            ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
        }
    }
}
