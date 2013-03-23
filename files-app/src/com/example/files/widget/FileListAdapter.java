package com.example.files.widget;

import static com.example.files.util.Objects.requires;

import java.io.File;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.media.ImageMap;
import com.example.files.util.FileSystem;

public final class FileListAdapter extends ArrayAdapter<File> {

    private final FileSystem mFileSystem;
    private final ImageMap mImageMap;

    public FileListAdapter(Context context) {
        this(context, FileSystem.INSTANCE, ImageMap.INSTANCE);
    }

    public FileListAdapter(Context context, FileSystem fs, ImageMap images) {
        super(context, R.layout.file_item);
        this.mFileSystem = requires(fs, "fs");
        this.mImageMap = requires(images, "images");
    }

    @Override
    public boolean isEnabled(int position) {
        return mFileSystem.hasPermissionToRead(getItem(position));
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        return updateView(super.getView(position, v, parent), getItem(position));
    }

    View updateView(View view, File file) {
        setEnabled(view, file);
        setText(view, file);
        setIcon(view, file);
        return view;
    }

    private void setEnabled(View view, File file) {
        view.setEnabled(mFileSystem.hasPermissionToRead(file));
    }

    private void setIcon(View view, File file) {
        ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(mImageMap.get(file), 0, 0, 0);
    }

    private void setText(View view, File file) {
        ((TextView) view).setText(file.getName());
    }
}
