package net.roosmaa.sample.localfood.ui;

import net.roosmaa.sample.localfood.R;
import net.roosmaa.sample.localfood.ui.fragment.LocationFragment;
import net.roosmaa.sample.localfood.ui.fragment.PlacesMapFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class PlacesMapActivity extends FragmentActivity
{
  @Override
  protected void onCreate(Bundle savedInstanceArgs)
  {
    super.onCreate(savedInstanceArgs);
    
    setContentView(R.layout.activity_places_map);
    
    Bundle extras = getIntent().getExtras();
    String placeId = extras.getString("PlaceId");
    
    if (savedInstanceArgs == null)
    {
      LocationFragment locationFragment = new LocationFragment();
      locationFragment.setArguments(extras.getBundle("LocationArguments"));
      
      Bundle args = new Bundle();
      args.putString("placeId", placeId);
      PlacesMapFragment mapFragment = new PlacesMapFragment();
      mapFragment.setArguments(args);
      
      getSupportFragmentManager()
          .beginTransaction()
          .add(locationFragment, LocationFragment.TAG)
          .add(R.id.fragment_places_map, mapFragment)
          .commit();
    }
  }
  
}
