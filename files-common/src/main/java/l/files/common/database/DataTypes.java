package l.files.common.database;

public final class DataTypes {
  private DataTypes() {}

  public static String booleanToString(boolean flag) {
    return String.valueOf(booleanToInt(flag));
  }

  public static int booleanToInt(boolean flag) {
    return flag ? 1 : 0;
  }

  public static boolean intToBoolean(int i) {
    return i != 0;
  }
}
