package net.roosmaa.sample.localfood.ui;

import net.roosmaa.sample.localfood.R;
import net.roosmaa.sample.localfood.ui.fragment.LocationFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class PlacesListActivity extends FragmentActivity
{
  @Override
  protected void onCreate(Bundle savedInstanceArgs)
  {
    super.onCreate(savedInstanceArgs);

    setContentView(R.layout.activity_places_list);

    if (savedInstanceArgs == null)
    {
      LocationFragment frag = new LocationFragment();
      frag.setArguments(getIntent().getBundleExtra("LocationArguments"));
      getSupportFragmentManager()
          .beginTransaction()
          .add(frag, LocationFragment.TAG)
          .commit();
    }
  }
}
