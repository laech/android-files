package l.files.ui;

import java.util.List;

public interface ListProvider<T> {

  int getCheckedItemCount();

  int getCheckedItemPosition();

  List<Integer> getCheckedItemPositions();

  T getCheckedItem();

  List<T> getCheckedItems();

}
