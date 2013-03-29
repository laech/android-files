package com.example.files.inject;

import android.app.Application;
import com.example.files.app.FilesActivity;
import com.example.files.app.FilesActivityHelper;
import com.example.files.app.FilesFragment;
import com.example.files.media.ImageMap;
import com.example.files.media.MediaDetector;
import com.example.files.media.MediaMap;
import com.example.files.util.FileSystem;
import com.example.files.widget.FilesAdapter;
import com.example.files.widget.Toaster;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(
    entryPoints = {
        FilesFragment.class,
        FilesActivity.class
    },
    complete = false
)
public final class FilesModule {

  @Provides @Singleton Bus provideBus() {
    return new Bus(ThreadEnforcer.MAIN);
  }

  @Provides @Singleton FileSystem provideFileSystem() {
    return new FileSystem();
  }

  @Provides @Singleton Toaster provideToaster() {
    return new Toaster();
  }

  @Provides @Singleton MediaDetector provideMediaDetector(Bus bus) {
    return new MediaDetector(bus);
  }

  @Provides @Singleton ImageMap provideImageMap() {
    return new ImageMap();
  }

  @Provides @Singleton MediaMap provideMediaMap() {
    return new MediaMap();
  }

  @Provides @Singleton FilesActivityHelper provideFilesActivityHelper(
      FileSystem fileSystem,
      MediaMap map,
      MediaDetector detector,
      Toaster toaster) {
    return new FilesActivityHelper(fileSystem, map, detector, toaster);
  }

  @Provides FilesAdapter provideFilesAdapter(
      Application context, FileSystem fileSystem, ImageMap images) {
    return new FilesAdapter(context, fileSystem, images);
  }
}

