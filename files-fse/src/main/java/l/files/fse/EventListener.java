package l.files.fse;

interface EventListener {

  /**
   * @see android.os.FileObserver#onEvent(int, String)
   */
  void onEvent(int event, String path);
}
