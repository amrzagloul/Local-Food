package net.roosmaa.sample.localfood.provider;

import net.roosmaa.sample.localfood.provider.FoodContract.PlacesColumns;
import net.roosmaa.sample.localfood.provider.FoodContract.Restaurants;
import net.roosmaa.sample.localfood.provider.FoodDatabase.Tables;
import net.roosmaa.sample.localfood.utils.QueryBuilder;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class FoodProvider extends ContentProvider
{
  private FoodDatabase mOpenHelper;
  
  private static final UriMatcher sUriMatcher = buildUriMatcher();
  
  private static final int RESTAURANTS = 100;
  private static final int RESTAURANTS_ID = 101;
  
  private static UriMatcher buildUriMatcher()
  {
    final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    final String authority = FoodContract.CONTENT_AUTHORITY;
    
    matcher.addURI(authority, "restaurants", RESTAURANTS);
    matcher.addURI(authority, "restaurants/*", RESTAURANTS_ID);
    
    return matcher;
  }
  
  @Override
  public String getType(Uri uri)
  {
    final int match = sUriMatcher.match(uri);
    switch (match)
    {
    case RESTAURANTS:
      return Restaurants.CONTENT_TYPE;
    case RESTAURANTS_ID:
      return Restaurants.CONTENT_ITEM_TYPE;
    default:
      throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
  }
  
  @Override
  public Uri insert(Uri uri, ContentValues values)
  {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    
    switch (match)
    {
    case RESTAURANTS:
      db.insertOrThrow(Tables.RESTAURANTS, null, values);
      getContext().getContentResolver().notifyChange(uri, null);
      return Restaurants
          .buildPlaceUri(values.getAsString(Restaurants.PLACE_ID));
    default:
      throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
  }
  
  @Override
  public boolean onCreate()
  {
    final Context ctx = getContext();
    mOpenHelper = new FoodDatabase(ctx);
    return true;
  }
  
  /**
   * Constructs a simple {@link QueryBuilder} that should be sufficient for
   * {@link #query}, {@link #update} and {@link #delete}.
   */
  private QueryBuilder simpleQuery(Uri uri, int match)
  {
    final QueryBuilder builder = new QueryBuilder();
    
    switch (match)
    {
    case RESTAURANTS:
      builder.table(Tables.RESTAURANTS);
    case RESTAURANTS_ID:
      final String placeId = Restaurants.getPlaceId(uri);
      builder.table(Tables.RESTAURANTS).where(PlacesColumns.PLACE_ID, placeId);
    }
    
    return builder;
  }
  
  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder)
  {
    final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    final int match = sUriMatcher.match(uri);
    final QueryBuilder builder = simpleQuery(uri, match);
    
    return builder.query(db, projection, sortOrder);
  }
  
  @Override
  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs)
  {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    final QueryBuilder builder = simpleQuery(uri, match);
    
    return builder.update(db, values);
  }
  
  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs)
  {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    final QueryBuilder builder = simpleQuery(uri, match);
    
    return builder.delete(db);
  }
  
}
