package net.roosmaa.sample.localfood.ui.fragment;

import net.roosmaa.sample.localfood.R;
import net.roosmaa.sample.localfood.ui.fragment.LocationFragment.StatusListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ActionBarFragment extends Fragment implements StatusListener
{
  private View mButtonRefresh;
  private TextView mTextStatus;
  private LocationFragment mLocationFragment;
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState)
  {
    View root = inflater.inflate(
        R.layout.fragment_action_bar, container, false);
    
    mButtonRefresh = root.findViewById(R.id.button_refresh);
    mTextStatus = (TextView) root.findViewById(R.id.text_status);
    
    mButtonRefresh.setOnClickListener(mButtonRefreshClicked);
    
    return root;
  }
  
  @Override
  public void onStart()
  {
    super.onStart();
    
    mLocationFragment = (LocationFragment)
        getFragmentManager().findFragmentByTag(LocationFragment.TAG);
    
    if (mLocationFragment == null)
    {
      mButtonRefresh.setVisibility(View.GONE);
      mTextStatus.setVisibility(View.INVISIBLE);
    }
    else
    {
      mButtonRefresh.setVisibility(View.VISIBLE);
      mTextStatus.setVisibility(View.VISIBLE);
      mLocationFragment.setStatusListener(this);
      syncStatus();
    }
  }
  
  @Override
  public void onStop()
  {
    super.onStop();
    
    if (mLocationFragment != null)
      mLocationFragment.setStatusListener(null);
  }
  
  private void syncStatus()
  {
    if (mLocationFragment == null)
      return;
    
    int status = mLocationFragment.getStatus();
    switch (status)
    {
    case LocationFragment.STATUS_IDLE:
      mTextStatus.setVisibility(View.INVISIBLE);
      break;
    case LocationFragment.STATUS_ACQUIRING_LOCATION:
      mTextStatus.setText(R.string.status_location);
      mTextStatus.setVisibility(View.VISIBLE);
      break;
    case LocationFragment.STATUS_FETCHING_DATA:
      mTextStatus.setText(R.string.status_data);
      mTextStatus.setVisibility(View.VISIBLE);
      break;
    }
  }
  
  @Override
  public void onStatusChanged(LocationFragment fragment)
  {
    syncStatus();
  }
  
  private OnClickListener mButtonRefreshClicked = new OnClickListener() {
    @Override
    public void onClick(View v)
    {
      if (mLocationFragment != null)
        mLocationFragment.refreshLocation(false);
    }
  };
}
