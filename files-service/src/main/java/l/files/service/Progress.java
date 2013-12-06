package l.files.service;

abstract class Progress {

  abstract String getNotificationContentTitle();

  String getNotificationContentText() {
    return null;
  }

  String getNotificationContentInfo() {
    return null;
  }

  float getNotificationProgressPercentage() {
    return 0;
  }
}
