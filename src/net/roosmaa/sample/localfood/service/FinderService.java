package net.roosmaa.sample.localfood.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.roosmaa.sample.localfood.R;
import net.roosmaa.sample.localfood.provider.FoodContract;
import net.roosmaa.sample.localfood.provider.FoodContract.Restaurants;
import net.roosmaa.sample.localfood.provider.FoodProvider;
import net.roosmaa.sample.localfood.utils.MobileHttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Service that downloads the near by places to the provided location and
 * updates the {@link FoodProvider} with the new information.
 */
public class FinderService extends IntentService
{
  private static final String TAG = "FinderService";
  
  public static final String EXTRA_STATUS_RECEIVER =
      "net.roosmaa.sample.localfood.extra.STATUS_RECEIVER";
  public static final String EXTRA_LONGITUDE =
      "net.roosmaa.sample.localfood.extra.LONGITUDE";
  public static final String EXTRA_LATITUDE =
      "net.roosmaa.sample.localfood.extra.LATITUDE";
  
  public static final int STATUS_RUNNING = 1;
  public static final int STATUS_ERROR = 2;
  public static final int STATUS_FINISHED = 3;
  
  private static final String API_URL =
      "https://maps.googleapis.com/maps/api/place/search/json"
          + "?location=%f,%f&radius=1610&types=restaurant&sensor=true&key=%s";
  
  public FinderService()
  {
    super(TAG);
  }
  
  @Override
  protected void onHandleIntent(Intent intent)
  {
    final ResultReceiver receiver = intent
        .getParcelableExtra(EXTRA_STATUS_RECEIVER);
    if (receiver != null)
      receiver.send(STATUS_RUNNING, Bundle.EMPTY);
    
    if (!intent.hasExtra(EXTRA_LONGITUDE) || !intent.hasExtra(EXTRA_LATITUDE))
    {
      final Bundle bundle = new Bundle();
      Log.e(TAG, "Called with no longitude and/or latitude.");
      bundle.putString(Intent.EXTRA_TEXT, "Missing longitude and/or latitude.");
      receiver.send(STATUS_ERROR, bundle);
      return;
    }
    
    final float latitude = intent.getFloatExtra(EXTRA_LATITUDE, 0);
    final float longitude = intent.getFloatExtra(EXTRA_LONGITUDE, 0);
    final String apiKey = getResources().getString(R.string.places_api_key);
    
    try
    {
      String data = fetchData(latitude, longitude, apiKey);
      processData(data);
      
      if (receiver != null)
        receiver.send(STATUS_FINISHED, Bundle.EMPTY);
    }
    catch (Exception e)
    {
      if (receiver != null)
      {
        final Bundle bundle = new Bundle();
        bundle.putString(Intent.EXTRA_TEXT, e.toString());
        receiver.send(STATUS_ERROR, bundle);
      }
    }
  }
  
  private String fetchData(float latitude, float longitude, String apiKey)
      throws IOException
  {
    HttpClient httpClient = MobileHttpClient.newInstance(
        "LocalFood/1.0", getBaseContext());
    HttpResponse response;
    
    try
    {
      HttpGet get = new HttpGet(
          String.format(API_URL, latitude, longitude, apiKey));
      response = httpClient.execute(get);
    }
    catch (IOException e)
    {
      Log.e(TAG, "Failed to communicate with the server.", e);
      throw e;
    }
    
    // Read the response:
    HttpEntity entity = response.getEntity();
    InputStream input = null;
    
    if (entity == null)
    {
      // TODO: Report error
    }
    
    try
    {
      input = entity.getContent();
      
      final InputStreamReader inputReader = new InputStreamReader(input,
            "UTF-8");
      final StringBuilder sb = new StringBuilder();
      
      char[] buf = new char[4096];
      while (true)
      {
        int length = inputReader.read(buf);
        if (length < 0)
          break;
        sb.append(buf, 0, length);
      }
      
      return sb.toString();
    }
    catch (IOException e)
    {
      Log.e(TAG, "Failed to retrieve the response from server.", e);
      throw e;
    }
    finally
    {
      // Clean up:
      try
      {
        if (input != null)
          input.close();
      }
      catch (IOException e)
      {
        Log.w(TAG, "Failed to close input stream.", e);
      }
      
      try
      {
        if (entity != null)
          entity.consumeContent();
      }
      catch (IOException e)
      {
        Log.w(TAG, "Failed to consome HttpEntity.", e);
      }
    }
  }
  
  private void processData(String json) throws JSONException, ParseException,
      RemoteException, OperationApplicationException
  {
    try
    {
      JSONObject root = new JSONObject(json);
      
      final String status = root.getString("status");
      if (status != "OK" && status != "ZERO_RESULTS")
      {
        Log.e(TAG, "Got an unexpected response from server: " + status);
        throw new ParseException("Got an unexpected response from server.");
      }
      
      final JSONArray results = root.getJSONArray("results");
      final ArrayList<ContentProviderOperation> batch =
          new ArrayList<ContentProviderOperation>();
      
      // Construct the database operations:
      batch.add(ContentProviderOperation.newDelete(Restaurants.CONTENT_URI)
          .build());
      
      for (int i = 0; i < results.length(); i++)
      {
        final JSONObject res = results.getJSONObject(i);
        final ContentProviderOperation.Builder builder =
            ContentProviderOperation.newInsert(Restaurants.CONTENT_URI);
        
        final JSONObject geo = res.getJSONObject("geometry");
        final JSONObject loc = geo.getJSONObject("location");
        final JSONArray typesArray = res.getJSONArray("types");
        
        StringBuilder types = new StringBuilder();
        for (int j = 0; j < typesArray.length(); j++)
        {
          final String type = typesArray.optString(j);
          if (types.length() > 0)
            types.append(",");
          types.append(type);
        }
        
        builder.withValue(Restaurants.PLACE_ID, res.getString("id"));
        builder.withValue(Restaurants.PLACE_LAT, loc.getDouble("lat"));
        builder.withValue(Restaurants.PLACE_LNG, loc.getDouble("lng"));
        builder.withValue(Restaurants.PLACE_ICON, res.optString("icon"));
        builder.withValue(Restaurants.PLACE_NAME, res.getString("name"));
        builder.withValue(Restaurants.PLACE_RATING, res.getDouble("rating"));
        builder.withValue(Restaurants.PLACE_REFERENCE,
            res.optString("reference"));
        builder.withValue(Restaurants.PLACE_TYPES, types);
        builder
            .withValue(Restaurants.PLACE_VICINITY, res.optString("vicinity"));
        
        batch.add(builder.build());
      }
      
      // Run the batch operation:
      getContentResolver().applyBatch(FoodContract.CONTENT_AUTHORITY, batch);
    }
    catch (JSONException e)
    {
      Log.e(TAG, "Failed to parse the server response.", e);
      throw e;
    }
    catch (RemoteException e)
    {
      Log.e(TAG, "Batch operation failed.", e);
      throw e;
    }
    catch (OperationApplicationException e)
    {
      Log.e(TAG, "Batch operation failed.", e);
      throw e;
    }
  }
}
