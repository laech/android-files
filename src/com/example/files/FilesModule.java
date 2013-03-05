package com.example.files;

import javax.inject.Singleton;

import com.example.files.ui.fragments.FileListFragment;
import com.example.files.util.FileSystem;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import dagger.Module;
import dagger.Provides;

@Module(
    entryPoints = {
        FileListFragment.class
    })
final class FilesModule {

  @Provides @Singleton Bus provideBus() {
    return new Bus(ThreadEnforcer.MAIN);
  }

  @Provides @Singleton FileSystem provideFileSystem() {
    return new FileSystem();
  }
}
