package l.files.operations;

public final class Permissions {

  /**
   * Permission required to receiver progress intents.
   */
  public static final String RECEIVE_PROGRESS =
      "l.files.operations.permission.RECEIVE_PROGRESS";

  /**
   * Permission required to broadcast progress intents to receivers.
   */
  public static final String SEND_PROGRESS =
      "l.files.operations.permission.SEND_PROGRESS";

  private Permissions() {}
}
