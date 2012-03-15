package net.roosmaa.sample.localfood.ui;

import net.roosmaa.sample.localfood.R;
import net.roosmaa.sample.localfood.ui.fragment.LocationFragment;
import net.roosmaa.sample.localfood.ui.fragment.PlacesMapFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class PlacesListActivity extends FragmentActivity
{
  @Override
  protected void onCreate(Bundle savedInstanceArgs)
  {
    super.onCreate(savedInstanceArgs);

    setContentView(R.layout.activity_places_list);

    if (savedInstanceArgs == null)
    {
      FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
      
      LocationFragment locationFragment = new LocationFragment();
      locationFragment.setArguments(getIntent().getBundleExtra("LocationArguments"));
      tx.add(locationFragment, LocationFragment.TAG);
      
      if (findViewById(R.id.fragment_places_map) != null)
      {
        PlacesMapFragment mapFragment = new PlacesMapFragment();
        tx.add(R.id.fragment_places_map, mapFragment);
      }
      
      tx.commit();
    }
  }
}
