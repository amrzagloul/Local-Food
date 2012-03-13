package net.roosmaa.sample.localfood.provider;

import net.roosmaa.sample.localfood.provider.FoodContract.PlacesColumns;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class FoodDatabase extends SQLiteOpenHelper
{
  private static final String TAG = "FoodDatabase";
  
  private static final String DATABASE_NAME = "food.db";
  private static final int DATABASE_VERSION = 1;
  
  interface Tables
  {
    String RESTAURANTS = "restaurants";
  }
  
  public FoodDatabase(Context context)
  {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
  
  @Override
  public void onCreate(SQLiteDatabase db)
  {
    db.execSQL("CREATE TABLE " + Tables.RESTAURANTS + " ("
        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + PlacesColumns.PLACE_ID + " TEXT NOT NULL,"
        + PlacesColumns.PLACE_LNG + " REAL NOT NULL,"
        + PlacesColumns.PLACE_LAT + " REAL NOT NULL,"
        + PlacesColumns.PLACE_ICON + " TEXT NOT NULL,"
        + PlacesColumns.PLACE_NAME + " TEXT NOT NULL,"
        + PlacesColumns.PLACE_RATING + " REAL NOT NULL,"
        + PlacesColumns.PLACE_REFERENCE + " TEXT NOT NULL,"
        + PlacesColumns.PLACE_TYPES + " TEXT NOT NULL,"
        + PlacesColumns.PLACE_VICINITY + " TEXT NOT NULL,"
        + "UNIQUE (" + PlacesColumns.PLACE_ID + ") ON CONFLICT REPLACE)");
  }
  
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {
    Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
    
    db.execSQL("DROP TABLE IS EXISTS " + Tables.RESTAURANTS);
    onCreate(db);
  }
}
