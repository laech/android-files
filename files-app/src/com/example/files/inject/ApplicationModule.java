package com.example.files.inject;

import android.app.Application;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

@Module
public final class ApplicationModule {

  private Application application;

  public ApplicationModule(Application application) {
    this.application = checkNotNull(application, "application");
  }

  @Provides @Singleton Application provideApplication() {
    return application;
  }
}
