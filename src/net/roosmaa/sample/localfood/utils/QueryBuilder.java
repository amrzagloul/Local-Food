package net.roosmaa.sample.localfood.utils;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * Helper class for building queries for {@link SQLiteDatabas}.
 */
public class QueryBuilder
{
  private String mTable = null;
  private StringBuilder mSelection = new StringBuilder();
  private ArrayList<String> mSelectionArgs = new ArrayList<String>();
  
  public QueryBuilder table(String table)
  {
    mTable = table;
    return this;
  }
  
  public QueryBuilder where(String selection, String... selectionArgs)
  {
    if (TextUtils.isEmpty(selection))
    {
      if (selectionArgs == null || selectionArgs.length == 0)
        return this;
      throw new IllegalArgumentException(
          "Valid selection required when arguments present.");
    }
    
    if (mSelection.length() > 0)
      mSelection.append(" AND ");
    
    mSelection.append("(").append(selection).append(")");
    if (selectionArgs != null)
    {
      for (String arg : selectionArgs)
        mSelectionArgs.add(arg);
    }
    
    return this;
  }
  
  public String getSelection()
  {
    return mSelection.toString();
  }
  
  public String[] getSelectionArgs()
  {
    return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
  }
  
  /**
   * Simple wrapper for the {@link SQLiteDatabase#query} using the internal
   * state as {@code WHERE} clause.
   */
  public Cursor query(SQLiteDatabase db, String[] columns, String orderBy)
  {
    return db.query(mTable, columns, getSelection(), getSelectionArgs(), null,
        null, orderBy);
  }
  
  /**
   * Simple wrapper for the {@link SQLiteDatabase#update} using the internal
   * state as {@code WHERE} clause.
   */
  public int update(SQLiteDatabase db, ContentValues values)
  {
    return db.update(mTable, values, getSelection(), getSelectionArgs());
  }
  
  /**
   * Simple wrapper for the {@link SQLiteDatabase#delete} using the internal
   * state as {@code WHERE} clause.
   */
  public int delete(SQLiteDatabase db)
  {
    return db.delete(mTable, getSelection(), getSelectionArgs());
  }
}
