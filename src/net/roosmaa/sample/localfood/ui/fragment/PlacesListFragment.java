package net.roosmaa.sample.localfood.ui.fragment;

import net.roosmaa.sample.localfood.R;
import net.roosmaa.sample.localfood.provider.FoodContract.Restaurants;
import net.roosmaa.sample.localfood.ui.PlacesMapActivity;
import net.roosmaa.sample.localfood.ui.fragment.PlacesMapFragment.SelectedPlaceListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

public class PlacesListFragment extends ListFragment implements
    LoaderManager.LoaderCallbacks<Cursor>, SelectedPlaceListener
{
  private PlacesMapFragment mMapFragment;
  private SimpleCursorAdapter mAdapter;
  private int mSelectedPosition;
  
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
  public void onSaveInstanceState(Bundle outState)
  {
    super.onSaveInstanceState(outState);
    outState.putInt("mSelectedPosition", mSelectedPosition);
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    
    mSelectedPosition = 0;
    
    if (savedInstanceState != null)
    {
      mSelectedPosition = savedInstanceState.getInt("mSelectedPosition", 0);
    }
  }
  
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
  public void onStart()
  {
    super.onStart();
    
    mMapFragment = (PlacesMapFragment)
        getFragmentManager().findFragmentById(R.id.fragment_places_map);
    
    if (mMapFragment != null)
    {
      getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      mMapFragment.setSelectedPlaceListener(this);
    }
    else
    {
      getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
    }
  }
  
  @Override
  public void onStop()
  {
    super.onStop();
    
    if (mMapFragment != null)
      mMapFragment.setSelectedPlaceListener(null);
  }
  
  private void showPlace(int position)
  {
    mSelectedPosition = position;
    
    // Get the place ID:
    if (mSelectedPosition >= mAdapter.getCount())
      return;
    final Cursor cur = (Cursor) mAdapter.getItem(position);
    if (cur == null)
      return;
    final int columnId = cur.getColumnIndex(Restaurants.PLACE_ID);
    final String placeId = cur.getString(columnId);
    
    // Display place
    if (mMapFragment != null)
    {
      getListView().setItemChecked(position, true);
      mMapFragment.setSelectedPlaceId(placeId);
    }
    else
    {
      final LocationFragment frag = (LocationFragment)
          getFragmentManager().findFragmentByTag(LocationFragment.TAG);
      
      Intent intent = new Intent(getActivity(), PlacesMapActivity.class);
      intent.putExtra("PlaceId", placeId);
      if (frag != null)
        intent.putExtra("LocationArguments", frag.getInstanceArguments());
      
      startActivity(intent);
    }
  }
  
  @Override
  public void onListItemClick(ListView l, View v, int position, long id)
  {
    showPlace(position);
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
    
    if (mMapFragment != null)
      showPlace(mSelectedPosition);
  }
  
  @Override
  public void onLoaderReset(Loader<Cursor> loader)
  {
    mAdapter.swapCursor(null);
  }

  @Override
  public void onSelectedPlaceChanged(PlacesMapFragment fragment)
  {
    final String placeId = fragment.getSelectedPlaceId();
    final Cursor cur = mAdapter.getCursor();
    if (cur == null || placeId == null)
      return;
    
    final int columnId = cur.getColumnIndex(Restaurants.PLACE_ID);
    
    cur.moveToPosition(-1);
    while (cur.moveToNext())
    {
      if (placeId.equals(cur.getString(columnId)))
      {
        showPlace(cur.getPosition());
        break;
      }
    }
  }
}
