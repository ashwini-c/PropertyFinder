package org.stevej.android.propertyfinder.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.stevej.android.propertyfinder.R;
import org.stevej.android.propertyfinder.fragments.FavouritePropertiesListFragment;
import org.stevej.android.propertyfinder.fragments.LocationTrackerFragment;
import org.stevej.android.propertyfinder.fragments.PlaceMenuFragment;
import org.stevej.android.propertyfinder.fragments.PlayServicesErrorDialogFragment;
import org.stevej.android.propertyfinder.fragments.PropertyDetailsFragment;
import org.stevej.android.propertyfinder.fragments.PropertyListFragment;
import org.stevej.android.propertyfinder.fragments.PropertyMapFragment;
import org.stevej.android.propertyfinder.fragments.ProviderDisabledDialogFragment;
import org.stevej.android.propertyfinder.interfaces.LocationTrackerListener;
import org.stevej.android.propertyfinder.interfaces.PropertySelectionListener;
import org.stevej.android.propertyfinder.interfaces.TradeMeResponseListener;
import org.stevej.android.propertyfinder.model.PlaceMenuEntry;
import org.stevej.android.propertyfinder.model.Property;
import org.stevej.android.propertyfinder.trademe.JSONParser;
import org.stevej.android.propertyfinder.trademe.TradeMeClientFragment;
import org.stevej.android.propertyfinder.utils.BitmapCache;
import org.stevej.android.propertyfinder.utils.CustomAnimator;
import org.stevej.android.propertyfinder.utils.TemporaryFileUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.common.ConnectionResult;

public class PropertyFinderActivity extends Activity implements TradeMeResponseListener, PropertySelectionListener,
		ActionBar.OnNavigationListener, LocationTrackerListener, OnFocusChangeListener, OnQueryTextListener {

	private static final String				TAG								= "PropertyFinderActivity";

	// tags to identify fragments
	private static final String				TRADEME_CLIENT_FRAGMENT_TAG		= "TradeMeClientFragment";
	private static final String				PROPERTY_LIST_FRAGMENT_TAG		= "PropertyListFragment";
	private static final String				FAVOURITES_LIST_FRAGMENT_TAG	= "FavouritesListFragment";
	private static final String				PROPERTY_DETAILS_FRAGMENT_TAG	= "PropertyDetailsFragment";
	private static final String				LOCATION_TRACKER_FRAGMENT_TAG	= "LocationTrackerFragment";
	private static final String				PLACE_MENU_FRAGMENT_TAG			= "PlaceMenuFragment";
	private static final String				PROPERTY_MAP_FRAGMENT_TAG		= "PropertyMapFragment";
	private static final String				ERROR_DIALOG_FRAGMENT_TAG		= "ErrorDialogFragment";
	private static final String				ALERT_DIALOG_FRAGMENT_TAG		= "AlertDialogFragment";

	// keys for saving instance state data
	private static final String				SELECTED_PLACE_IDX				= "SELECTED_PLACE_IDX";
	private static final String				SEARCH_VIEW_TEXT				= "SEARCH_VIEW_TEXT";
	private static final String				SEARCH_VIEW_EXPANDED			= "SEARCH_VIEW_EXPANDED";

	// no initial nav menu selection
	private static final int				NO_SELECTION					= -1;

	// intent action we handle when user selects a place suggestion from the search widget
	private static final String				ADD_PLACE_ACTION				= "org.stevej.android.propertyfinder.action.ADD_PLACE";

	// stores position of currently selected place from menu so that we can store/restore it on config change
	private int								selected_place_position			= NO_SELECTION;

	// stores device orientation
	private int								orientation;

	// fragment manager and fragments
	private FragmentManager					fragment_manager;
	private PropertyListFragment			property_list_fragment			= null;
	private FavouritePropertiesListFragment	favourites_list_fragment		= null;
	private PropertyDetailsFragment			property_details_fragment		= null;
	private PropertyMapFragment				property_map_fragment			= null;
	private TradeMeClientFragment			trademe_client					= null;
	private LocationTrackerFragment			location_tracker				= null;
	private PlaceMenuFragment				place_menu						= null;

	// we set these up but aren't using them (yet)
	private ShareActionProvider				share_action_provider;
	private Intent							share_intent;

	// holds action bar search widget text that we save/restore on configuration change
	private CharSequence					query_text						= "";

	// holds action bar search widget state that we save/restore on configuration change
	boolean									search_view_expanded			= false;

	// action bar components
	private MenuItem						favourite_menu_item;
	private MenuItem						search_menu_item;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "\n\nonCreate()");
		super.onCreate(savedInstanceState);

		// set UI layout from resource file
		super.setContentView(R.layout.property_finder);

		// set preferences defaults if they have never been set in the past
		PreferenceManager.setDefaultValues(this, R.xml.settings_all, true);

		// get the activity's fragment manager
		fragment_manager = getFragmentManager();

		// get current orientation
		orientation = getResources().getConfiguration().orientation;

		// add the trademe client, property list, property details, place menu and location tracker fragments to this activity
		addFragments();

		// if we have saved state after a configuration change we can restore the previous suburb selection and search widget state in the
		// menu
		if (savedInstanceState != null) {
			selected_place_position = savedInstanceState.getInt(SELECTED_PLACE_IDX);

			if (savedInstanceState.containsKey(SEARCH_VIEW_TEXT)) {
				query_text = savedInstanceState.getCharSequence(SEARCH_VIEW_TEXT);
			}

			if (savedInstanceState.containsKey(SEARCH_VIEW_EXPANDED)) {
				search_view_expanded = savedInstanceState.getBoolean(SEARCH_VIEW_EXPANDED);
			}
		}

		// set up the intent for sharing data (sharing not yet implemented)
		configureShareIntent();
	}

	private void handleIntent(Intent intent) {
		Log.d(TAG, "handleIntent : " + intent.toString());

		String intent_action = intent.getAction();
		if (ADD_PLACE_ACTION.equals(intent_action)) {
			Uri data_uri = intent.getData();

			ContentResolver cr = getContentResolver();
			Cursor cursor = cr.query(data_uri, null, null, null, null);

			if (cursor.getCount() != 0) {
				cursor.moveToFirst();
				String place_name = cursor.getString(cursor.getColumnIndexOrThrow("Name"));
				String place_type = cursor.getString(cursor.getColumnIndexOrThrow("Type"));
				String place_id = cursor.getString(cursor.getColumnIndexOrThrow("LocationId"));
				String district_id = cursor.getString(cursor.getColumnIndexOrThrow("DistrictID"));
				String region_id = cursor.getString(cursor.getColumnIndexOrThrow("RegionId"));

				PlaceMenuEntry entry = new PlaceMenuEntry(place_name, region_id, district_id, place_id, place_type, null);

				if (!place_menu.containsEntry(entry.place_name, entry.place_id)) {
					selected_place_position = place_menu.addEntry(entry);
				}
			}
			search_menu_item.collapseActionView();
		} else if (Intent.ACTION_SEND.equals(intent_action)) {
			Log.d(TAG, "SHARE_ACTION");

			String listing_id = intent.getStringExtra("listing_id");
			String sent_text = intent.getStringExtra(Intent.EXTRA_TEXT);
			Toast.makeText(this, "We need to share : " + listing_id + "\n" + sent_text, Toast.LENGTH_LONG).show();
			
		} 
	}

	/*
	 * Invoked when the system has identified this activity as able to handle a particular intent. In this case it is when the user selects
	 * a search suggestion (a place) and the required action is to add it to the action bar nav menu. The intent data will be of the form
	 * content://org.stevej.android.propertyfinder.providers.TradeMePlaceProvider/places/<id> identifying the content provider that can
	 * provide the place data and the id of the place to add
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent : " + intent.toString());
		handleIntent(intent);
	}

	/*
	 * get references to the fragments. If we haven't already got the fragments (this is a 'clean' start of the activity) then create the
	 * fragment instances and add them to their UI containers
	 */
	private void addFragments() {
		Log.d(TAG, "addFragments()");

		property_map_fragment = (PropertyMapFragment) fragment_manager.findFragmentByTag(PROPERTY_MAP_FRAGMENT_TAG);
		property_list_fragment = (PropertyListFragment) fragment_manager.findFragmentByTag(PROPERTY_LIST_FRAGMENT_TAG);
		favourites_list_fragment = (FavouritePropertiesListFragment) fragment_manager.findFragmentByTag(FAVOURITES_LIST_FRAGMENT_TAG);
		property_details_fragment = (PropertyDetailsFragment) fragment_manager.findFragmentByTag(PROPERTY_DETAILS_FRAGMENT_TAG);
		trademe_client = (TradeMeClientFragment) fragment_manager.findFragmentByTag(TRADEME_CLIENT_FRAGMENT_TAG);
		location_tracker = (LocationTrackerFragment) fragment_manager.findFragmentByTag(LOCATION_TRACKER_FRAGMENT_TAG);
		place_menu = (PlaceMenuFragment) fragment_manager.findFragmentByTag(PLACE_MENU_FRAGMENT_TAG);

		FragmentTransaction ft = fragment_manager.beginTransaction();

		if (place_menu == null) {
			place_menu = new PlaceMenuFragment();
			ft.add(place_menu, PLACE_MENU_FRAGMENT_TAG);
		}

		if (location_tracker == null) {
			location_tracker = new LocationTrackerFragment();
			ft.add(location_tracker, LOCATION_TRACKER_FRAGMENT_TAG);
		}

		if (trademe_client == null) {
			trademe_client = new TradeMeClientFragment();
			ft.add(trademe_client, TRADEME_CLIENT_FRAGMENT_TAG);
		}

		if (property_list_fragment == null) {
			property_list_fragment = new PropertyListFragment();
			ft.add(R.id.property_list_container, property_list_fragment, PROPERTY_LIST_FRAGMENT_TAG);
		}

		if (favourites_list_fragment == null) {
			favourites_list_fragment = new FavouritePropertiesListFragment();
			ft.add(R.id.property_list_container, favourites_list_fragment, FAVOURITES_LIST_FRAGMENT_TAG);
		}

		if (property_details_fragment == null) {
			property_details_fragment = new PropertyDetailsFragment();
			ft.add(R.id.property_details_container, property_details_fragment, PROPERTY_DETAILS_FRAGMENT_TAG);
		}

		if (property_map_fragment == null) {
			property_map_fragment = new PropertyMapFragment();
			ft.add(R.id.property_map_container, property_map_fragment, PROPERTY_MAP_FRAGMENT_TAG);
		}

		ft.commit();
		fragment_manager.executePendingTransactions();
	}

	/*
	 * We will eventually add sharing functionality via the Share ActionBar item. Create an initial Intent that we'll use to launch the app
	 * that will be used to share data.
	 */
	private void configureShareIntent() {
		share_intent = new Intent(Intent.ACTION_SEND);
		share_intent.setType("text/plain");
	}

	/*
	 * Configure the ActionBar appearance and behaviour. Instead of a predefined list of items use our PlaceMenu's adapter to provide the
	 * entries for the ActionBar navigation drop down list. Register this activity to handle navigation selections. Set the initial
	 * selection.
	 */
	private void configureActionBar() {
		Log.d(TAG, "configureActionBar()");

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		if (place_menu != null) {
			actionBar.setListNavigationCallbacks(place_menu.getAdapter(), this);
		}

		actionBar.setSelectedNavigationItem(selected_place_position);
	}

	/******************************************
	 * 
	 * Handlers for responses returned by the requests to the Trademe service
	 * 
	 ******************************************/

	// Parse the trademe response and update the list fragment with the new data
	@Override
	public void onTradeMePropertyListResponse(JSONObject json_object) {
		Log.d(TAG, "onTradeMePropertyListResponse()");
		if (json_object != null) {
			property_list_fragment.update(JSONParser.parsePropertyListJSON(json_object));
		} else {
			property_list_fragment.clear();
			Toast.makeText(this, "Could not load properties", Toast.LENGTH_LONG).show();
		}

		View property_map_container = findViewById(R.id.property_map_container);
		if (property_map_container.isShown()) {
			property_map_fragment.setPropertyList(property_list_fragment.getPropertyList(), property_list_fragment.getSelectedProperty());
		}
	}

	// not needed yet. We'll use it when we need more info about a property (eg its photos)
	@Override
	public void onTradeMePropertyDetailsResponse(JSONObject json_object) {
		Log.d(TAG, "onTradeMePropertyDetailsResponse()");
		Property p = JSONParser.parsePropertyDetailsJSON(json_object);

		Property updated_property;

		PlaceMenuEntry entry = place_menu.getEntry(selected_place_position);
		if (entry.place_name.equals("Favourites")) {
			updated_property = favourites_list_fragment.updateProperty(p);
		} else {
			updated_property = property_list_fragment.updateProperty(p);
		}
		if (updated_property != null) {
			property_details_fragment.update(updated_property);
		}

		Log.d(TAG, "onTradeMePropertyDetailsResponse() : " + p.photo_thumb_urls.toString());
		Log.d(TAG, "onTradeMePropertyDetailsResponse() : " + p.photo_large_urls.toString());
		
	}

	/******************************************
	 * 
	 * User input handlers
	 * 
	 ******************************************/

	// hide the search widget if it loses focus so that the action bar title is visible
	@Override
	public void onFocusChange(View arg0, boolean has_focus) {
		if (!has_focus) {
			search_menu_item.collapseActionView();
		}
	}

	// record changes to the search query so that we can put it into savedInstanceState
	@Override
	public boolean onQueryTextChange(String newText) {
		query_text = newText;
		return true;
	}

	// we don't need to initiate a search - we just want a selected suggestion
	@Override
	public boolean onQueryTextSubmit(String query) {
		return true;
	}

	/*
	 * ActionBar drop down nav list selection handler. Issue a request to the trademe service for properties in the given place, or display
	 * favourites
	 */
	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		Log.d(TAG, "onNavigationItemSelected : " + position);

		View property_details_container = findViewById(R.id.property_details_container);
		if (orientation == Configuration.ORIENTATION_PORTRAIT && property_details_container.isShown()) {
			CustomAnimator.reversePrevious();
		}

		selected_place_position = position;
		PlaceMenuEntry entry = place_menu.getEntry(position);

		if (entry.place_name.equals("Favourites")) {
			Log.d(TAG, "onNavigationItemSelected : showing favourites");
			FragmentTransaction ft = fragment_manager.beginTransaction();

			ft.hide(property_list_fragment);
			ft.show(favourites_list_fragment);
			ft.commit();

			// we can update the map immediately because we have the favourites in the DB
			View property_map_container = findViewById(R.id.property_map_container);
			if (property_map_container.isShown() && property_map_fragment != null) {
				property_map_fragment.setPropertyList(favourites_list_fragment.getPropertyList(),
						favourites_list_fragment.getSelectedProperty());
			}
		} else {
			Log.d(TAG, "onNavigationItemSelected : showing " + entry.place_name);
			FragmentTransaction ft = fragment_manager.beginTransaction();
			ft.hide(favourites_list_fragment);
			ft.show(property_list_fragment);
			ft.commit();
			if (!entry.place_name.equals("Shared")) {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
				String num_to_load = Integer.toString(preferences.getInt("pref_num_to_load", 10));
				if (trademe_client != null) {
					trademe_client.getPropertyList(entry, 5, num_to_load);
				}
			}
			// we will update the map when we get the response

		}
		if (property_list_fragment != null && !entry.place_name.equals("Shared")) {
			property_list_fragment.setIsLoading(true);
		}

		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			property_details_fragment.clear();
		}

		return true;
	}

	/*
	 * ActionBar items selection handler
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_sort_alpha:
				property_list_fragment.sortByTitle();
				return true;
			case R.id.action_sort_price_asc:
				property_list_fragment.sortByPriceAsc();
				return true;
			case R.id.action_sort_price_desc:
				property_list_fragment.sortByPriceDesc();
				return true;
			case R.id.action_legal:
				Toast.makeText(this, "Legal", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.action_map:
				showPropertyMap();
				return true;
			case R.id.action_single_screen_settings:
				startActivity(new Intent(this, SingleScreenSettingsActivity.class));
				return true;
			case R.id.action_favourite:
				PlaceMenuEntry entry = place_menu.getEntry(selected_place_position);
				Property property = null;
				if (entry.place_name.equals("Favourites")) {
					property = favourites_list_fragment.getSelectedProperty();
				} else {
					property = property_list_fragment.getSelectedProperty();
				}

				if (property != null) {
					if (favourites_list_fragment.isFavourite(property)) {
						favourites_list_fragment.removeProperty(property);
						favourite_menu_item.setIcon(android.R.drawable.btn_star_big_off);
					} else {
						favourites_list_fragment.addProperty(property);
						favourite_menu_item.setIcon(android.R.drawable.btn_star_big_on);
					}
				}
				return true;
			case R.id.action_clear_favourites:
				favourites_list_fragment.clear();
				return true;
			case R.id.action_dump_sensors:
				SensorManager sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

				List<Sensor> device_sensors = sensor_manager.getSensorList(Sensor.TYPE_ALL);

				for (int i = 0; i < device_sensors.size(); i++) {
					Sensor sensor = device_sensors.get(i);
					Log.d(TAG, "      name : " + sensor.getName());
					Log.d(TAG, "    vendor : " + sensor.getVendor());
					Log.d(TAG, "      type : " + sensor.getType());
					Log.d(TAG, " max range : " + sensor.getMaximumRange());
					Log.d(TAG, "     power : " + sensor.getPower());
					Log.d(TAG, " min delay : " + sensor.getMinDelay());
					Log.d(TAG, "   version : " + sensor.getVersion());
					Log.d(TAG, "resolution : " + sensor.getResolution());
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * Reverse the previous animation if we have one (eg display details fragment or display map fragment). Otherwise use the default
	 * behaviour.
	 */
	@Override
	public void onBackPressed() {
		if (CustomAnimator.hasHistory()) {
			CustomAnimator.reversePrevious();
		} else {
			super.onBackPressed();
		}
	}

	/*
	 * Invoked from property list fragment or favourites list fragment when item is clicked
	 */
	@Override
	public void onPropertySelected(Property property) {
		trademe_client.getPropertyDetails(Integer.toString(property.ListingID));
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			showPropertyDetails();
		}

		property_map_fragment.updateSelection(property);

		if (favourites_list_fragment.isFavourite(property)) {
			favourite_menu_item.setIcon(android.R.drawable.btn_star_big_on);
		} else {
			favourite_menu_item.setIcon(android.R.drawable.btn_star_big_off);
		}

		Bundle b = share_intent.getExtras();
		if (b != null) {
			Uri uri = (Uri) b.get(Intent.EXTRA_STREAM);
			if (uri != null) {
				TemporaryFileUtils.deleteTempBitmapFile(uri);
			}
		}
		Uri thumb_uri = TemporaryFileUtils.createTempBitmapFile(this, property.ThumbURL, getBitmapCache());

		share_intent.putExtra("listing_id", Integer.toString(property.ListingID));
		share_intent.putExtra(Intent.EXTRA_TEXT, property.Title + "\n" + property.PriceDisplay + "\n" + property.Address + "\n");
		share_intent.putExtra(Intent.EXTRA_STREAM, thumb_uri);
		share_action_provider.setShareIntent(share_intent);
	}

	/******************************************
	 * 
	 * Utility methods
	 * 
	 ******************************************/

	// slide the details fragment in and the list fragment out
	private void showPropertyDetails() {
		View property_list_container = findViewById(R.id.property_list_container);
		View property_details_container = findViewById(R.id.property_details_container);

		CustomAnimator.slide(property_details_container, property_list_container, CustomAnimator.DIRECTION_LEFT, 400);
	}

	// slide the map fragment in and either the list fragment or details fragment out
	// set the map's list of displayed properties from either a place list or the favourites list
	private void showPropertyMap() {
		View out_container = null;
		View property_map_container = findViewById(R.id.property_map_container);
		View property_details_container = findViewById(R.id.property_details_container);
		View property_list_container = findViewById(R.id.property_list_container);

		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			if (property_list_container.isShown()) {
				out_container = property_list_container;
			} else if (property_details_container.isShown()) {
				out_container = property_details_container;
			}
		} else {
			if (property_details_container.isShown()) {
				out_container = property_details_container;
			}
		}
		if (out_container != null) {
			CustomAnimator.slide(property_map_container, out_container, CustomAnimator.DIRECTION_LEFT, 400);
		}

		ArrayList<Property> properties = new ArrayList<Property>();
		Property selected_property = null;
		if (property_list_fragment.isVisible()) {
			properties = property_list_fragment.getPropertyList();
			selected_property = property_list_fragment.getSelectedProperty();
		} else if (favourites_list_fragment.isVisible()) {
			properties = favourites_list_fragment.getPropertyList();
			selected_property = favourites_list_fragment.getSelectedProperty();
		}

		property_map_fragment.setPropertyList(properties, selected_property);
	}

	// provide the image loader
	public ImageLoader getImageLoader() {
		if (trademe_client != null) {
			return trademe_client.getImageLoader();
		} else {
			return null;
		}
	}

	// provide the request queue
	public BitmapCache getBitmapCache() {
		if (trademe_client != null) {
			return trademe_client.getBitmapCache();
		} else {
			return null;
		}
	}

	/******************************************
	 * 
	 * Overridden lifecycle methods
	 * 
	 ******************************************/

	/*
	 * Pause receipt of location updates.
	 */
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean use_location = preferences.getBoolean("pref_allow_location", true);

		if (use_location && location_tracker != null) {
			location_tracker.pauseUpdates();
		}
	}

	// now the UI exists and is visible to the user. We can configure the action bar and catch up with location updates
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		configureActionBar();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean use_location = preferences.getBoolean("pref_allow_location", true);

		if (use_location && location_tracker != null) {
			location_tracker.resumeUpdates();
		}

		handleIntent(getIntent());
	}

	// make sure all pending network requests are cancelled when this activity stops
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		if (trademe_client != null) {
			trademe_client.cancelAllRequests();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 * 
	 * Save the current place selection in the action bar nav menu, the search widget text and whether it is shown
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		int selected_suburb_idx = getActionBar().getSelectedNavigationIndex();
		outState.putInt(SELECTED_PLACE_IDX, selected_suburb_idx);

		outState.putCharSequence(SEARCH_VIEW_TEXT, query_text);

		if (search_menu_item != null) {
			outState.putBoolean(SEARCH_VIEW_EXPANDED, search_menu_item.isActionViewExpanded());
		}

		super.onSaveInstanceState(outState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * 
	 * Configure the action bar action entries. Set up the search widget to to use the configuration in searchable.xml which enables search
	 * suggestions as the user types.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu()");

		getMenuInflater().inflate(R.menu.action_bar, menu);

		share_action_provider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
		share_action_provider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		share_action_provider.setShareIntent(share_intent);

		favourite_menu_item = (MenuItem) menu.findItem(R.id.action_favourite);

		search_menu_item = (MenuItem) menu.findItem(R.id.action_search);
		SearchView search_view = (SearchView) search_menu_item.getActionView();

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		ComponentName activity_name = getComponentName();
		SearchableInfo searchable_info = searchManager.getSearchableInfo(activity_name);
		search_view.setSearchableInfo(searchable_info);

		search_view.setSubmitButtonEnabled(false);
		search_view.setQueryHint("Suburb name");

		// if it was expanded before a config change, expand it again and hide the keyboard
		if (search_view_expanded) {
			search_menu_item.expandActionView();
			search_view.clearFocus();
			search_view.setQuery(query_text, false);
		}

		search_view.setOnQueryTextListener(this);
		search_view.setOnQueryTextFocusChangeListener(this);

		return true;
	}

	/**************************************************************
	 * 
	 * Location related callbacks for the LocationTrackerFragment
	 * 
	 *************************************************************/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.stevej.android.propertyfinder.fragments.LocationTrackerFragment.LocationTrackerListener#onTrackerConnected()
	 * 
	 * There has been a successful connection to Google Play Services/Location Services Start receiving location updates
	 */
	public void onTrackerConnected() {
		Log.d(TAG, "onTrackerConnected()");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean use_location = preferences.getBoolean("pref_allow_location", true);

		if (use_location) {
			location_tracker.startTracking();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.stevej.android.propertyfinder.fragments.LocationTrackerFragment.LocationTrackerListener#onLocationChanged(android.location.Location
	 * )
	 * 
	 * The location tracker has provided an updated location for the 'Nearby' menu entry. Create and add it if it doesn't exist, otherwise
	 * update it.
	 */
	public void onLocationChanged(Location new_location) {
		Log.d(TAG, "onLocationChanged()");

		if (place_menu == null) {
			return;
		}
		if (!place_menu.containsEntry("Nearby")) {
			PlaceMenuEntry nearby = new PlaceMenuEntry("Nearby", null, null, null, null, new_location);
			place_menu.addEntry(1, nearby);
		} else {
			place_menu.getEntry("Nearby").location = new_location;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.stevej.android.propertyfinder.fragments.LocationTrackerFragment.LocationTrackerListener#onDisabledLocationProvider(int)
	 * 
	 * Invoked when the tracker has identified that at least one location source (GPS, WiFi/mobile network) is disabled. Alert the user if
	 * necessary.
	 */
	public void onDisabledLocationProvider(int provider_state) {
		Log.d(TAG, "onDisabledLocationProvider()");

		// only show the alert if the user has not selected 'Dont show again' in a previous dialog, or chosen to not see them in the app
		// Settings
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean show_alerts = preferences.getBoolean("pref_location_alerts", true);
		if (!show_alerts) {
			return;
		}

		// we may be showing a previous dialog which is now out of date. Dismiss it.
		ProviderDisabledDialogFragment pddf = (ProviderDisabledDialogFragment) fragment_manager
				.findFragmentByTag(ALERT_DIALOG_FRAGMENT_TAG);
		if (pddf != null) {
			pddf.dismiss();
		}

		// create and show a new dialog, providing it with the current state of the location providers
		pddf = new ProviderDisabledDialogFragment();
		Bundle args = new Bundle();
		args.putInt("provider_state", provider_state);
		pddf.setArguments(args);
		pddf.show(fragment_manager, ALERT_DIALOG_FRAGMENT_TAG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 * 
	 * If Play Services was unavailable and there was a possible resolution identified by the Play Services framework we will have started
	 * the resolution action. This will provide a result to this method.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		Log.d(TAG, "onActivityResult()");
		switch (requestCode) {
			case LocationTrackerFragment.CONNECTION_FAILURE_RESOLUTION_REQUEST:
				switch (resultCode) {
					case Activity.RESULT_OK:
						break;
					default:
						break;
				}
			default:
				break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.stevej.android.propertyfinder.fragments.LocationTrackerFragment.LocationTrackerListener#onServiceUnavailable(com.google.android
	 * .gms.common. ConnectionResult)
	 * 
	 * Invoked when the tracker has identified that Play Services is not available. Either launch the the provided resolution action or show
	 * a dialog alerting the user.
	 */
	public void onServiceUnavailable(ConnectionResult connection_result, int resolution_request_id) {
		Log.d(TAG, "onServiceUnavailable()");

		// dismiss an existing dialog that may be visible
		PlayServicesErrorDialogFragment pedf = (PlayServicesErrorDialogFragment) fragment_manager
				.findFragmentByTag(ERROR_DIALOG_FRAGMENT_TAG);
		if (pedf != null) {
			pedf.dismiss();
		}

		if (connection_result.hasResolution()) {
			try {
				connection_result.startResolutionForResult(this, resolution_request_id);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			pedf = new PlayServicesErrorDialogFragment();
			Bundle args = new Bundle();
			args.putInt("error_code", connection_result.getErrorCode());
			pedf.setArguments(args);
			pedf.show(fragment_manager, ERROR_DIALOG_FRAGMENT_TAG);
		}
	}

}
