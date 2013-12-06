package l.files.service;

abstract class DeleteBaseProgress extends Progress {

  final int total;
  final int remaining;

  DeleteBaseProgress(int total, int remaining) {
    this.total = total;
    this.remaining = remaining;
  }

  @Override float getNotificationProgressPercentage() {
    return (total - remaining) / (float) total;
  }
}
