package com.example.files.widget;

import android.widget.ArrayAdapter;

import java.util.Collection;

public final class ArrayAdapters {

  @SuppressWarnings("unchecked")
  public static void removeAll(ArrayAdapter<?> adapter, Collection<?> items) {
    if (items.isEmpty()) return;

    adapter.setNotifyOnChange(false);
    for (Object item : items) {
      ((ArrayAdapter<Object>) adapter).remove(item);
    }
    adapter.notifyDataSetChanged();
  }

  private ArrayAdapters() {
  }
}
