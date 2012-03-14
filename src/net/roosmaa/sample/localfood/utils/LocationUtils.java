package net.roosmaa.sample.localfood.utils;

import android.location.Location;

public class LocationUtils
{
  private static final int TWO_MINUTES = 1000 * 60 * 2;
  
  public static boolean isBetter(Location old, Location location)
  {
    if (old == null)
      return true;
    
    // Check whether the new location fix is newer or older
    long timeDelta = location.getTime() - old.getTime();
    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
    boolean isNewer = timeDelta > 0;
    
    // If it's been more than two minutes since the current location, use the
    // new location because the user has likely moved
    if (isSignificantlyNewer)
      return true;
    // If the new location is more than two minutes older, it must be worse
    else if (isSignificantlyOlder)
      return false;
    
    // Check whether the new location fix is more or less accurate
    int accuracyDelta = (int) (location.getAccuracy() - old.getAccuracy());
    boolean isLessAccurate = accuracyDelta > 0;
    boolean isMoreAccurate = accuracyDelta < 0;
    boolean isSignificantlyLessAccurate = accuracyDelta > 200;
    
    // Check if the old and new location are from the same provider
    boolean isFromSameProvider = isSameProvider(location, old);
    
    // Determine location quality using a combination of timeliness and accuracy
    if (isMoreAccurate)
      return true;
    else if (isNewer && !isLessAccurate)
      return true;
    else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
      return true;
    return false;
  }
  
  public static boolean isSameProvider(Location lhs, Location rhs)
  {
    String p1 = lhs.getProvider();
    String p2 = rhs.getProvider();
    if (p1 == null)
      return p2 == null;
    return p1.equals(p2);
  }
}
