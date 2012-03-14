package net.roosmaa.sample.localfood.ui.fragment;

import com.google.android.maps.MapView;

import net.roosmaa.sample.localfood.R;
import net.roosmaa.sample.localfood.provider.FoodContract.Restaurants;
import net.roosmaa.sample.localfood.ui.GoogleMapsActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class PlacesMapFragment extends ActivityManagerFragment implements
    LoaderManager.LoaderCallbacks<Cursor>
{
  private Cursor mCursor;
  private MapView mMapView;
  
  private static final String[] CURSOR_PROJECTION = new String[] {
      Restaurants.PLACE_ID,
      Restaurants.PLACE_LAT,
      Restaurants.PLACE_LNG,
      Restaurants.PLACE_NAME,
  };
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState)
  {
    Intent intent = new Intent(
        getActivity(), GoogleMapsActivity.class);
    @SuppressWarnings("deprecation")
    Window wnd = getLocalActivityManager().startActivity("GoogleMaps", intent);
    
    View root = wnd.getDecorView();
    root.setVisibility(View.VISIBLE);
    root.setFocusableInTouchMode(true);
    ((ViewGroup) root).setDescendantFocusability(
        ViewGroup.FOCUS_AFTER_DESCENDANTS);
    
    mMapView = (MapView) root.findViewById(R.id.map_view);
    
    return root;
  }
  
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
