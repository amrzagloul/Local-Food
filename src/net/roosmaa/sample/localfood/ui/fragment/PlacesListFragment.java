package net.roosmaa.sample.localfood.ui.fragment;

import net.roosmaa.sample.localfood.R;
import net.roosmaa.sample.localfood.provider.FoodContract.Restaurants;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;

public class PlacesListFragment extends ListFragment implements
    LoaderManager.LoaderCallbacks<Cursor>
{
  private SimpleCursorAdapter mAdapter;
  
  private static final String[] CURSOR_PROJECTION = new String[] {
      Restaurants._ID,
      Restaurants.PLACE_ID,
      Restaurants.PLACE_NAME,
      Restaurants.PLACE_RATING,
      Restaurants.PLACE_VICINITY,
  };
  
  private static final String[] ADAPTER_MAP_FROM = new String[] {
      Restaurants.PLACE_NAME,
      Restaurants.PLACE_RATING,
      Restaurants.PLACE_VICINITY,
  };
  
  private static final int[] ADAPTER_MAP_TO = new int[] {
      R.id.text_name,
      R.id.text_score,
      R.id.text_address,
  };
  
  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);
    
    mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_place,
        null, ADAPTER_MAP_FROM, ADAPTER_MAP_TO, 0);
    setListAdapter(mAdapter);
    
    setListShown(false);
    
    getLoaderManager().initLoader(0, null, this);
  }
  
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args)
  {
    return new CursorLoader(getActivity(), Restaurants.CONTENT_URI,
        CURSOR_PROJECTION, null, null, Restaurants.DEFAULT_SORT);
  }
  
  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data)
  {
    mAdapter.swapCursor(data);
    
    if (isResumed())
      setListShown(true);
    else
      setListShownNoAnimation(true);
  }
  
  @Override
  public void onLoaderReset(Loader<Cursor> loader)
  {
    mAdapter.swapCursor(null);
  }
}
