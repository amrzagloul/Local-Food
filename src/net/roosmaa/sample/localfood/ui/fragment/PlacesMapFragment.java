package net.roosmaa.sample.localfood.ui.fragment;

import net.roosmaa.sample.localfood.provider.FoodContract.Restaurants;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

public class PlacesMapFragment extends Fragment implements
    LoaderManager.LoaderCallbacks<Cursor>
{
  private Cursor mCursor;
  
  private static final String[] CURSOR_PROJECTION = new String[] {
      Restaurants.PLACE_ID,
      Restaurants.PLACE_LAT,
      Restaurants.PLACE_LNG,
      Restaurants.PLACE_NAME,
  };
  
  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);
    
    getLoaderManager().initLoader(0, null, this);
  }
  
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args)
  {
    return new CursorLoader(getActivity(), Restaurants.CONTENT_URI,
        CURSOR_PROJECTION, null, null, null);
  }
  
  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data)
  {
    mCursor = data;
  }
  
  @Override
  public void onLoaderReset(Loader<Cursor> loader)
  {
    mCursor = null;
  }
}
