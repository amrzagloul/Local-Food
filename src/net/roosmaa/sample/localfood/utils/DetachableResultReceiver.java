package net.roosmaa.sample.localfood.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

public class DetachableResultReceiver extends ResultReceiver
{
  private static final String TAG = "DetachableResultReceiver";
  
  private Receiver mReceiver;
  
  public DetachableResultReceiver(Handler handler)
  {
    super(handler);
  }
  
  public void setReceiver(Receiver receiver)
  {
    mReceiver = receiver;
  }
  
  @Override
  protected void onReceiveResult(int resultCode, Bundle resultData)
  {
    if (mReceiver != null)
      mReceiver.onReceiveResult(resultCode, resultData);
    else
      Log.w(TAG, "Dropping result for code " + resultCode + ": "
              + resultData.toString());
  }
  
  public interface Receiver
  {
    public void onReceiveResult(int resultCode, Bundle resultData);
  }
}
