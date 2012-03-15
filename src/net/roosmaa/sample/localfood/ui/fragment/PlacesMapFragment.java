package net.roosmaa.sample.localfood.ui.fragment;

import java.util.ArrayList;

import net.roosmaa.sample.localfood.R;
import net.roosmaa.sample.localfood.provider.FoodContract.Restaurants;
import net.roosmaa.sample.localfood.ui.GoogleMapsActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class PlacesMapFragment extends ActivityManagerFragment implements
    LoaderManager.LoaderCallbacks<Cursor>
{
  public interface SelectedPlaceListener {
    void onSelectedPlaceChanged(PlacesMapFragment fragment);
  }
  
  private MapView mMapView;
  private MyLocationOverlay mLocationOverlay;
  private PlacesOverlay mOverlay;
  
  private static final String[] CURSOR_PROJECTION = new String[] {
      Restaurants.PLACE_ID,
      Restaurants.PLACE_LAT,
      Restaurants.PLACE_LNG,
      Restaurants.PLACE_NAME,
      Restaurants.PLACE_VICINITY,
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
    
    mMapView = (MapView) root.findViewById(R.id.map_view);
    mMapView.setBuiltInZoomControls(true);
    mLocationOverlay = new MyLocationOverlay(getActivity(), mMapView);
    mMapView.getOverlays().add(mLocationOverlay);
    mOverlay = new PlacesOverlay(this, getActivity());
    mMapView.getOverlays().add(mOverlay);
    
    return root;
  }
  
  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);
    final Bundle args = getArguments();
    
    if (savedInstanceState != null)
      mOverlay.setSelectedId(savedInstanceState.getString("selectedPlaceId"));
    else if (args != null)
      mOverlay.setSelectedId(args.getString("placeId"));
    
    getLoaderManager().initLoader(0, null, this);
  }
  
  @Override
  public void onSaveInstanceState(Bundle outState)
  {
    super.onSaveInstanceState(outState);
    
    outState.putString("selectedPlaceId", mOverlay.getSelectedId());
  }
  
  @Override
  public void onResume()
  {
    super.onResume();
    
    mLocationOverlay.enableMyLocation();
    mLocationOverlay.enableCompass();
  }
  
  @Override
  public void onPause()
  {
    super.onPause();
    
    mLocationOverlay.disableMyLocation();
    mLocationOverlay.disableCompass();
  }
  
  public void setSelectedPlaceListener(SelectedPlaceListener listener)
  {
    mOverlay.setSelectedPlaceListener(listener);
  }
  
  public String getSelectedPlaceId()
  {
    return mOverlay.getSelectedId();
  }
  
  public void setSelectedPlaceId(String placeId)
  {
    final String oldPlaceId = mOverlay.getSelectedId();
    
    if ((placeId == null && oldPlaceId == null) || placeId.equals(oldPlaceId))
      return;
    
    mOverlay.setSelectedId(placeId);
    resetMapViewport();
  }
  
  private void setCursor(Cursor cursor)
  {
    mOverlay.setCursor(cursor);
    resetMapViewport();
    mMapView.invalidate();
  }
  
  private void resetMapViewport()
  {
    final LocationFragment locationFragment = (LocationFragment)
        getFragmentManager().findFragmentByTag(LocationFragment.TAG);
    final MapController controller = mMapView.getController();
    final OverlayItem selectedPlace = mOverlay.getSelectedItem();
    if (selectedPlace != null)
    {
      controller.animateTo(selectedPlace.getPoint());
      controller.setZoom(17);
    }
    else if (locationFragment != null)
    {
      controller.animateTo(new GeoPoint(
          (int) (locationFragment.getLatitude() * 1e6),
          (int) (locationFragment.getLongitude() * 1e6)));
      controller.setZoom(17);
    }
    
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
    setCursor(data);
  }
  
  @Override
  public void onLoaderReset(Loader<Cursor> loader)
  {
    setCursor(null);
  }
  
  private class PlacesOverlay extends ItemizedOverlay<OverlayItem>
  {
    private final ArrayList<String> mPlaceIds = new ArrayList<String>();
    private PlacesMapFragment mFragment;
    private Context mContext;
    private Drawable mSelectedMarker;
    private Cursor mCursor;
    private int mIdxName;
    private int mIdxVicinity;
    private int mIdxLatitude;
    private int mIdxLongitude;
    private String mSelectedId;
    private SelectedPlaceListener mSelectedPlaceListener;
    
    public PlacesOverlay(PlacesMapFragment fragment, Context ctx)
    {
      super(boundCenterBottom(
          ctx.getResources().getDrawable(R.drawable.marker_default)));
      mFragment = fragment;
      mContext = ctx;
      mSelectedMarker = boundCenterBottom(
          ctx.getResources().getDrawable(R.drawable.marker_selected));
    }
    
    public void setSelectedId(String id)
    {
      // Unselect old item:
      int pos = mPlaceIds.indexOf(mSelectedId);
      if (pos != -1)
        getItem(pos).setMarker(null);
      
      mSelectedId = id;
      
      // Select new item:
      pos = mPlaceIds.indexOf(mSelectedId);
      if (pos != -1)
        getItem(pos).setMarker(mSelectedMarker);
      
      if (mSelectedPlaceListener != null)
        mSelectedPlaceListener.onSelectedPlaceChanged(mFragment);
    }
    
    public OverlayItem getSelectedItem()
    {
      int pos = mPlaceIds.indexOf(mSelectedId);
      if (pos != -1)
        return getItem(pos);
      return null;
    }
    
    public String getSelectedId()
    {
      return mSelectedId;
    }
    
    public void setSelectedPlaceListener(SelectedPlaceListener listener)
    {
      mSelectedPlaceListener = listener;
    }
    
    public void setCursor(Cursor cursor)
    {
      mPlaceIds.clear();
      mCursor = cursor;
      
      if (mCursor != null)
      {
        int idxId = mCursor.getColumnIndex(Restaurants.PLACE_ID);
        mIdxName = mCursor.getColumnIndex(Restaurants.PLACE_NAME);
        mIdxVicinity = mCursor.getColumnIndex(Restaurants.PLACE_VICINITY);
        mIdxLatitude = mCursor.getColumnIndex(Restaurants.PLACE_LAT);
        mIdxLongitude = mCursor.getColumnIndex(Restaurants.PLACE_LNG);
        
        // Populate cache:
        mCursor.moveToPosition(-1);
        while (mCursor.moveToNext())
          mPlaceIds.add(mCursor.getString(idxId));
      }
      
      populate();
    }
    
    @Override
    protected OverlayItem createItem(int position)
    {
      mCursor.moveToPosition(position);
      String name = mCursor.getString(mIdxName);
      String vicinity = mCursor.getString(mIdxVicinity);
      double lat = mCursor.getDouble(mIdxLatitude);
      double lng = mCursor.getDouble(mIdxLongitude);
      
      OverlayItem item = new OverlayItem(
          new GeoPoint((int) (lat * 1e6), (int) (lng * 1e6)),
          name, vicinity);
      
      int selPosition = mPlaceIds.indexOf(mSelectedId);
      if (selPosition == position)
        item.setMarker(mSelectedMarker);
      
      return item;
    }
    
    @Override
    public int size()
    {
      if (mCursor == null)
        return 0;
      return mCursor.getCount();
    }
    
    @Override
    protected boolean onTap(int position)
    {
      // Unselect old item:
      int selPosition = mPlaceIds.indexOf(mSelectedId);
      if (selPosition != -1)
        getItem(selPosition).setMarker(null);
      
      // Select new item:
      final OverlayItem item = getItem(position);
      item.setMarker(mSelectedMarker);
      mSelectedId = mPlaceIds.get(position);
      
      if (mSelectedPlaceListener != null)
        mSelectedPlaceListener.onSelectedPlaceChanged(mFragment);
      
      final String text = String.format(
          "%s - %s", item.getTitle(), item.getSnippet());
      Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
      
      return true;
    }
  }
}
