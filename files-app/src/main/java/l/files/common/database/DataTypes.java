package l.files.common.database;

public final class DataTypes {
  private DataTypes() {}

  /**
   * Converts the flag to a string.
   * <p/>
   * If true returns "1", otherwise returns "0".
   */
  public static String booleanToString(boolean flag) {
    return String.valueOf(booleanToInt(flag));
  }

  /**
   * Converts the flag to an int.
   * <p/>
   * If true returns 1, otherwise returns 0.
   */
  public static int booleanToInt(boolean flag) {
    return flag ? 1 : 0;
  }

  /**
   * Converts the int to boolean.
   * <p/>
   * If 1 returns true, 0 returns false, otherwise {@link
   * IllegalArgumentException} will be thrown.
   */
  public static boolean intToBoolean(int i) {
    if (i == 1) {
      return true;
    } else if (i == 0) {
      return false;
    } else {
      throw new IllegalArgumentException(String.valueOf(i));
    }
  }
}
