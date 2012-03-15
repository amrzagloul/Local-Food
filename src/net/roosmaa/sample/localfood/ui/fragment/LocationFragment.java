package net.roosmaa.sample.localfood.ui.fragment;

import java.util.List;

import net.roosmaa.sample.localfood.service.FinderService;
import net.roosmaa.sample.localfood.utils.DetachableResultReceiver;
import net.roosmaa.sample.localfood.utils.DetachableResultReceiver.Receiver;
import net.roosmaa.sample.localfood.utils.LocationUtils;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Non-UI fragment to manage location resolving and communication with the
 * {@link FinderService}.
 */
public class LocationFragment extends Fragment implements Receiver,
    LocationListener
{
  public static final String TAG = "LocationFragment";
  
  private static final String PREFS_FILE = "LocationCache";
  private static final String PREF_LATITUDE = "latitude";
  private static final String PREF_LONGITUDE = "longitude";
  private static final String PREF_ACQUIRE_TIME = "acquireTime";
  
  private static final int LOCATION_TOTAL_TIME = 15000;
  private static final int LOCATION_MIN_TIME = 1000;
  private static final int LOCATION_MIN_DISTANCE = 100;
  private static final int SERVICE_MONITOR_INTERVAL = 1000;
  
  public static final int STATUS_IDLE = 0;
  public static final int STATUS_ACQUIRING_LOCATION = 1;
  public static final int STATUS_FETCHING_DATA = 2;
  
  public interface StatusListener
  {
    void onStatusChanged(LocationFragment fragment);
  }
  
  private final Handler mHandler = new Handler();
  private final DetachableResultReceiver mReceiver =
      new DetachableResultReceiver(mHandler);
  private Criteria mProviderCriteria;
  private Location mLocation;
  private int mStatus;
  private StatusListener mStatusListener;
  private long mAcquireTime;
  private float mLongitude;
  private float mLatitude;
  
  /**
   * Reference counter for the number of {@link FinderService} invocations we
   * have made.
   * 
   * When this is 0, but {@link #mStatus} is {@link #STATUS_FETCHING_DATA} then
   * we need to manually monitor the state of the {@link FinderService}. This is
   * usually the case when the information fetching was in progress and the user
   * navigated into another activity.
   * 
   * See also {@link #checkUntrackedFetch}.
   */
  private int mFetchOperations;
  
  /**
   * Gets a {@link Bundle} that can be used as arguments to create a new
   * {@link LocationFragment} with an identical state.
   */
  public Bundle getInstanceArguments()
  {
    Bundle bundle = new Bundle();
    bundle.putInt("mStatus", mStatus);
    bundle.putParcelable("mLocation", mLocation);
    return bundle;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    mReceiver.setReceiver(this);
    
    mStatus = STATUS_IDLE;
    mFetchOperations = 0;
    
    mProviderCriteria = new Criteria();
    mProviderCriteria.setAltitudeRequired(false);
    mProviderCriteria.setSpeedRequired(false);
    mProviderCriteria.setBearingRequired(false);
    mProviderCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
    mProviderCriteria.setPowerRequirement(Criteria.POWER_MEDIUM);
    
    // Restore state partially from the arguments:
    Bundle args = getArguments();
    if (args != null)
    {
      mStatus = args.getInt("mStatus", mStatus);
      mLocation = args.getParcelable("mLocation");
    }
    
    // Restore state partially from the savedInstanceState:
    if (savedInstanceState != null)
    {
      mStatus = savedInstanceState.getInt("mStatus", mStatus);
      Location loc = savedInstanceState.getParcelable("mLocation");
      if (loc != null)
        mLocation = loc;
    }
    
    reloadCached();
  }
  
  @Override
  public void onSaveInstanceState(Bundle outState)
  {
    super.onSaveInstanceState(outState);
    outState.putInt("mStatus", mStatus);
    outState.putParcelable("mLocation", mLocation);
  }
  
  @Override
  public void onStart()
  {
    super.onStart();
    checkUntrackedFetch();
  }
  
  @Override
  public void onResume()
  {
    super.onResume();
    
    // Read preferences:
    long prevAcquireTime = mAcquireTime;
    reloadCached();
    
    if (mStatus == STATUS_ACQUIRING_LOCATION)
    {
      if (prevAcquireTime != mAcquireTime)
      {
        // We the latest location somewhere else already:
        setStatus(STATUS_IDLE);
      }
      else
      {
        // Restart listening for the location:
        final LocationManager mgr = (LocationManager)
            getActivity().getSystemService(Context.LOCATION_SERVICE);
        
        startAcquiringLocation(mgr);
      }
    }
    // Get new location when we don't have a location and we aren't doing
    // anything:
    else if (mStatus == STATUS_IDLE && mLocation == null)
    {
      refreshLocation(true);
    }
  }
  
  @Override
  public void onPause()
  {
    super.onPause();
    
    // Cancel listening for the location:
    if (mStatus == STATUS_ACQUIRING_LOCATION)
    {
      final LocationManager mgr = (LocationManager)
          getActivity().getSystemService(Context.LOCATION_SERVICE);
      
      cancelAcquiringLocation(mgr);
    }
  }
  
  @Override
  public void onStop()
  {
    super.onStop();
    mHandler.removeCallbacks(mServiceMonitor);
  }
  
  @Override
  public void onDestroy()
  {
    super.onDestroy();
    mReceiver.setReceiver(null);
  }
  
  private void reloadCached()
  {
    SharedPreferences cache = getActivity().getSharedPreferences(PREFS_FILE, 0);
    mAcquireTime = cache.getLong(PREF_ACQUIRE_TIME, 0);
    mLatitude = cache.getFloat(PREF_LATITUDE, 0);
    mLongitude = cache.getFloat(PREF_LONGITUDE, 0);
  }
  
  public void refreshLocation(boolean useCached)
  {
    // Already refreshing, ignore this request:
    if (mStatus != STATUS_IDLE)
      return;
    
    final LocationManager mgr = (LocationManager)
        getActivity().getSystemService(Context.LOCATION_SERVICE);
    
    setStatus(STATUS_ACQUIRING_LOCATION);
    
    if (useCached)
    {
      // Check if the cached location is better than the one we have:
      final List<String> providers = mgr.getProviders(mProviderCriteria, true);
      boolean gotBetterLocation = false;
      
      for (String provider : providers)
      {
        if (setLocation(mgr.getLastKnownLocation(provider)))
          gotBetterLocation = true;
      }
      
      // Load new places from the internet while we're getting an even better
      // location estimate.
      if (gotBetterLocation)
        startFetchingData();
    }
    
    startAcquiringLocation(mgr);
  }
  
  /**
   * Checks if the {@link FinderService} is running without being ref. counted
   * by {@link #mFetchOperations}.
   */
  private void checkUntrackedFetch()
  {
    if (mStatus == STATUS_FETCHING_DATA && mFetchOperations < 1)
    {
      if (isFinderServiceRunning())
        mHandler.postDelayed(mServiceMonitor, SERVICE_MONITOR_INTERVAL);
      else
        setStatus(STATUS_IDLE);
    }
  }
  
  /** Starts listening for current location. */
  private void startAcquiringLocation(LocationManager mgr)
  {
    if (mStatus != STATUS_ACQUIRING_LOCATION)
      return;
    
    mgr.requestLocationUpdates(LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE,
        mProviderCriteria, this, null);
    mHandler.postDelayed(mLocationTimeout, LOCATION_TOTAL_TIME);
  }
  
  /** Cancels listening for current location. */
  private void cancelAcquiringLocation(LocationManager mgr)
  {
    mHandler.removeCallbacks(mLocationTimeout);
    mgr.removeUpdates(LocationFragment.this);
  }
  
  private void finishAcquiringLocation()
  {
    if (mStatus != STATUS_ACQUIRING_LOCATION)
      return;
    
    final LocationManager mgr = (LocationManager)
        getActivity().getSystemService(Context.LOCATION_SERVICE);
    
    cancelAcquiringLocation(mgr);
    
    if (mLocation == null)
    {
      Log.w(TAG, "Failed to acquire location.");
      setStatus(STATUS_IDLE);
      return;
    }
    
    // Fetch data:
    setStatus(STATUS_FETCHING_DATA);
    startFetchingData();
  }
  
  private void startFetchingData()
  {
    final Activity activity = getActivity();
    if (activity == null)
    {
      Log.e(TAG,
          "Failed to start fetching data as there is no activity attached.");
      return;
    }
    
    mFetchOperations += 1;
    
    Intent intent = new Intent(activity, FinderService.class);
    intent.putExtra(FinderService.EXTRA_STATUS_RECEIVER, mReceiver);
    intent.putExtra(FinderService.EXTRA_LATITUDE, mLocation.getLatitude());
    intent.putExtra(FinderService.EXTRA_LONGITUDE, mLocation.getLongitude());
    activity.startService(intent);
  }
  
  private void finishFetchingData()
  {
    mFetchOperations -= 1;
    
    if (mFetchOperations < 1 && mStatus == STATUS_FETCHING_DATA)
    {
      mFetchOperations = 0;
      setStatus(STATUS_IDLE);
    }
  }
  
  private boolean isFinderServiceRunning()
  {
    final ActivityManager mgr = (ActivityManager) getActivity()
        .getSystemService(Context.ACTIVITY_SERVICE);
    final String finderService = FinderService.class.getName();
    
    for (RunningServiceInfo service : mgr.getRunningServices(Integer.MAX_VALUE))
    {
      if (finderService.equals(service.service.getClassName()))
        return true;
    }
    
    return false;
  }
  
  private boolean setLocation(Location location)
  {
    if (location != null && LocationUtils.isBetter(mLocation, location))
    {
      mLocation = location;
      
      // Cache the data:
      mAcquireTime = mLocation.getTime();
      mLatitude = (float) mLocation.getLatitude();
      mLongitude = (float) mLocation.getLongitude();
      SharedPreferences cache = getActivity().getSharedPreferences(PREFS_FILE,
          0);
      cache.edit()
           .putLong(PREF_ACQUIRE_TIME, mAcquireTime)
           .putFloat(PREF_LATITUDE, mLatitude)
           .putFloat(PREF_LONGITUDE, mLongitude)
           .commit();
      
      return true;
    }
    return false;
  }
  
  public int getStatus()
  {
    return mStatus;
  }
  
  public double getLongitude()
  {
    if (mLocation == null || mLocation.getTime() < mAcquireTime)
      return mLongitude;
    return mLocation.getLongitude();
  }
  
  public double getLatitude()
  {
    if (mLocation == null || mLocation.getTime() < mAcquireTime)
      return mLatitude;
    return mLocation.getLatitude();
  }
  
  private void setStatus(int status)
  {
    if (mStatus != status)
    {
      mStatus = status;
      if (mStatusListener != null)
        mStatusListener.onStatusChanged(this);
    }
  }
  
  public void setStatusListener(StatusListener listener)
  {
    mStatusListener = listener;
  }
  
  @Override
  public void onReceiveResult(int resultCode, Bundle resultData)
  {
    if (resultCode == FinderService.STATUS_RUNNING)
      return;
    
    finishFetchingData();
  }
  
  @Override
  public void onLocationChanged(Location location)
  {
    setLocation(location);
  }
  
  @Override
  public void onProviderDisabled(String provider)
  {
  }
  
  @Override
  public void onProviderEnabled(String provider)
  {
  }
  
  @Override
  public void onStatusChanged(String provider, int status, Bundle extras)
  {
  }
  
  private Runnable mLocationTimeout = new Runnable()
  {
    @Override
    public void run()
    {
      if (getActivity() != null)
        finishAcquiringLocation();
    }
  };
  
  private Runnable mServiceMonitor = new Runnable() {
    @Override
    public void run()
    {
      if (getActivity() != null)
        checkUntrackedFetch();
    }
  };
}
