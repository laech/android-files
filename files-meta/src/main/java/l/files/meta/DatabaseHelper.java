package l.files.meta;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static java.lang.String.format;
import static l.files.meta.MetaContract.Meta;

final class DatabaseHelper extends SQLiteOpenHelper {

  static final String TABLE_META = "meta";

  private static final String DB_NAME = "meta.db";
  private static final int DB_VERSION = 1;

  public DatabaseHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    createMetaTable(db);
  }

  private void createMetaTable(SQLiteDatabase db) {
    db.execSQL("create table " + TABLE_META + "(" +
        Meta.ID + " text not null primary key," +
        Meta.FILE_URI + " text not null unique on conflict replace," +
        Meta.DIRECTORY_URI + " text not null," +
        Meta.MIME + " text," +
        Meta.WIDTH + " integer," +
        Meta.HEIGHT + " integer)");
    db.execSQL(format(
        "create index idx_%1$s on %2$s(%1$s)", Meta.DIRECTORY_URI, TABLE_META));
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
