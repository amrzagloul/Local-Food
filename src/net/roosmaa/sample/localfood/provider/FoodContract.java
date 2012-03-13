package net.roosmaa.sample.localfood.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class FoodContract
{
  interface PlacesColumns
  {
    /** Contains a unique stable identifier denoting this place. */
    String PLACE_ID = "place_id";
    /** Longitude for the location. */
    String PLACE_LNG = "place_longitude";
    /** Latitude for the location. */
    String PLACE_LAT = "place_latitude";
    /**
     * Contains the URL of a recommended icon which may be displayed to the user
     * when indicating this result.
     */
    String PLACE_ICON = "place_icon";
    /** Contains the human-readable name for the returned result. */
    String PLACE_NAME = "place_name";
    /** Contains the Place's rating, from 0.0 to 5.0, based on user reviews. */
    String PLACE_RATING = "place_rating";
    /**
     * Contains a unique token that you can use to retrieve additional
     * information about this place in a Place Details request.
     */
    String PLACE_REFERENCE = "place_reference";
    /**
     * Contains a comma separated list of feature types describing the given
     * place.
     */
    String PLACE_TYPES = "place_types";
    /** Contains a feature name of a nearby location. */
    String PLACE_VICINITY = "place_vicinity";
  }
  
  public static final String CONTENT_AUTHORITY = "net.roosmaa.sample.localfood";
  
  private static final Uri BASE_CONTENT_URI = Uri.parse("content://"
      + CONTENT_AUTHORITY);
  
  private static final String PATH_RESTAURANTS = "restaurants";
  
  public static class Restaurants implements PlacesColumns, BaseColumns
  {
    public static final Uri CONTENT_URI =
        BASE_CONTENT_URI.buildUpon().appendPath(PATH_RESTAURANTS).build();
    
    public static final String CONTENT_TYPE =
        "vnd.android.cursor.dir/vnd.localfood.restaurant";
    public static final String CONTENT_ITEM_TYPE =
        "vnd.android.cursor.item/vnd.localfood.restaurant";
    
    public static final String DEFAULT_SORT = PlacesColumns.PLACE_RATING
        + " DESC";
    
    /** Build {@link Uri} for requested {@link #PLACE_ID}. */
    public static Uri buildPlaceUri(String placeId)
    {
      return CONTENT_URI.buildUpon().appendPath(placeId).build();
    }
    
    /** Read {@link #PLACE_ID} from {@link Restaurants} {@link Uri}. */
    public static String getPlaceId(Uri uri)
    {
      return uri.getPathSegments().get(1);
    }
  }
  
  private FoodContract()
  {
  }
}
