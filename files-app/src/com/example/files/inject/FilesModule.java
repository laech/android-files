package com.example.files.inject;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import javax.inject.Singleton;

import android.app.Application;
import android.content.SharedPreferences;
import com.example.files.app.SidebarFragment;
import com.example.files.app.FilesActivity;
import com.example.files.app.FilesActivityHelper;
import com.example.files.app.FilesFragment;
import com.example.files.app.Settings;
import com.example.files.media.ImageMap;
import com.example.files.media.MediaDetector;
import com.example.files.media.MediaMap;
import com.example.files.util.FileSystem;
import com.example.files.app.FilesAdapter;
import com.example.files.widget.Toaster;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import dagger.Module;
import dagger.Provides;

@Module(
    entryPoints = {
        SidebarFragment.class,
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

  @Provides @Singleton SharedPreferences provideSharedPreferences(
      Application application) {
    return getDefaultSharedPreferences(application);
  }

  @Provides @Singleton Settings provideSettings(
      Application application, SharedPreferences preferences) {
    return new Settings(application, preferences);
  }
}

