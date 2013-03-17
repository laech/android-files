package com.example.files;

import javax.inject.Singleton;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.files.media.ImageMap;
import com.example.files.media.MediaMap;
import com.example.files.ui.ActivityStarter;
import com.example.files.ui.Toaster;
import com.example.files.ui.adapters.FileListAdapter;
import com.example.files.util.FileSystem;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import dagger.Module;
import dagger.Provides;

@Module(
    entryPoints = FilesApp.class // FIXME
)
final class FilesModule {

  @Provides @Singleton ActivityStarter provideActivityStarter() {
    return new ActivityStarter() {
      @Override public void startActivity(Context context, Intent intent) {
        context.startActivity(intent);
      }
    };
  }

  @Provides @Singleton Bus provideBus() {
    return new Bus(ThreadEnforcer.MAIN);
  }

  @Provides @Singleton FileSystem provideFileSystem() {
    return new FileSystem();
  }

  @Provides @Singleton ImageMap provideImageMap() {
    return new ImageMap();
  }

  @Provides @Singleton MediaMap provideMediaMap() {
    return new MediaMap();
  }

  @Provides @Singleton Toaster provideToaster() {
    return new Toaster() {
      @Override public void toast(Context context, int resId, int duration) {
        Toast.makeText(context, resId, duration).show();
      }
    };
  }

  @Provides Context provideContext() {
    return FilesApp.getInstance();
  }

  @Provides FileListAdapter provideFileListAdapter(Context context, FileSystem fs, ImageMap imgs) {
    return new FileListAdapter(context, fs, imgs);
  }
}
