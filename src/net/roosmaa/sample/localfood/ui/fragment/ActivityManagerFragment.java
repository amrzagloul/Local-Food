package net.roosmaa.sample.localfood.ui.fragment;

import android.app.LocalActivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;

@SuppressWarnings("deprecation")
public class ActivityManagerFragment extends Fragment
{
  private static final String STATE_BUNDLE_KEY = "localActivityManagerState";
  
  private LocalActivityManager mManager;
  
  protected LocalActivityManager getLocalActivityManager() {
    return mManager;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    
    Bundle state = null;
    if (savedInstanceState != null)
      state = savedInstanceState.getBundle(STATE_BUNDLE_KEY);
    
    mManager = new LocalActivityManager(getActivity(), true);
    mManager.dispatchCreate(state);
  }
  
  @Override
  public void onSaveInstanceState(Bundle outState)
  {
    super.onSaveInstanceState(outState);
    outState.putBundle(STATE_BUNDLE_KEY, mManager.saveInstanceState());
  }
  
  @Override
  public void onResume()
  {
    super.onResume();
    mManager.dispatchResume();
  }
  
  @Override
  public void onPause()
  {
    super.onPause();
    mManager.dispatchPause(getActivity().isFinishing());
  }
  
  @Override
  public void onStop()
  {
    super.onStop();
    mManager.dispatchStop();
  }
  
  @Override
  public void onDestroy()
  {
    super.onDestroy();
    mManager.dispatchDestroy(getActivity().isFinishing());
  }
}
