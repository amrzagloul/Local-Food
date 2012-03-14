package net.roosmaa.sample.localfood.ui;

import net.roosmaa.sample.localfood.R;
import android.os.Bundle;

import com.google.android.maps.MapActivity;

public class GoogleMapsActivity extends MapActivity
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_google_maps);
  }
  
  @Override
  protected boolean isRouteDisplayed()
  {
    return false;
  }
}
