package org.stevej.android.propertyfinder.fragments;

import java.util.ArrayList;

import org.stevej.android.propertyfinder.activities.PropertyFinderActivity;
import org.stevej.android.propertyfinder.adapters.FavouritePropertiesAdapter;
import org.stevej.android.propertyfinder.database.FavouritePropertiesDBHelper;
import org.stevej.android.propertyfinder.interfaces.PropertySelectionListener;
import org.stevej.android.propertyfinder.model.Property;
import org.stevej.android.propertyfinder.providers.FavouritePropertiesProvider;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class FavouritePropertiesListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String			TAG						= "FavouritePropertiesListFragment";
	private static final int			NO_SELECTION			= -1;

	private PropertySelectionListener	property_selection_listener;
	private FavouritePropertiesAdapter	property_list_adapter;

	private int							selected_item_position	= NO_SELECTION;
	// These are the Favourites columns that we will retrieve.
	private static final String[]		FAVOURITES_PROJECTION	= new String[] { BaseColumns._ID, "Title", "PriceDisplay", "PictureHref",
			"ListingId", "Category", "PriceNumeric", "HasGallery", "HasLocation", "HasAgency", "Latitude", "Longitude", "Address",
			"PropertyType", "AgencyName", "AgencyPhone", "Body" };

	/*
	 * requires empty constructor
	 */
	public FavouritePropertiesListFragment() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onAttach(android.app.Activity)
	 * 
	 * This fragment has now been added to the activity, which we use as the listener for property list selections
	 */
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach()");
		super.onAttach(activity);
		try {
			property_selection_listener = (PropertySelectionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement PropertySelectionListener");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreate(android.os.Bundle)
	 * 
	 * Set up the data/list view adapter
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);

		// stops onDestroy() and onCreate() being called when the parent
		// activity is destroyed/recreated on configuration change
		setRetainInstance(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated()");
		super.onActivityCreated(savedInstanceState);

		property_list_adapter = new FavouritePropertiesAdapter(getActivity(), ((PropertyFinderActivity) getActivity()).getImageLoader());
		getActivity().getLoaderManager().initLoader(0, null, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onResume()
	 * 
	 * The fragment is visible and 'alive'. Now we can do UI operations.
	 */
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();

		getListView().setAdapter(property_list_adapter);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		Log.d(TAG, "onResume : adapter count = " + property_list_adapter.getCount() + ", selected_item_position = "
				+ selected_item_position);

		setIsLoading(false);

		if (property_list_adapter.getCount() > 0 && selected_item_position != NO_SELECTION) {
			getListView().setItemChecked(selected_item_position, true);
			getListView().smoothScrollToPositionFromTop(selected_item_position, 100, 100);
		}
	}

	public void setIsLoading(boolean is_loading) {
		setListShown(!is_loading);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		selected_item_position = position;

		getListView().setItemChecked(position, true);

		Cursor c = getActivity().getContentResolver().query(ContentUris.withAppendedId(FavouritePropertiesProvider.CONTENT_URI, id), null,
				"", null, "");

		c.moveToFirst();
		property_selection_listener.onPropertySelected(FavouritePropertiesDBHelper.getProperty(c));
	}

	public Property getProperty(int listing_id) {
		Cursor c = getActivity().getContentResolver().query(
				ContentUris.withAppendedId(FavouritePropertiesProvider.CONTENT_URI, listing_id), null, "", null, "");
		if (c.getCount() > 0) {
			c.moveToFirst();
			return FavouritePropertiesDBHelper.getProperty(c);
		} else {
			return null;
		}

	}

	/*
	 * Return the currently selected property
	 */
	public Property getSelectedProperty() {
		if (property_list_adapter.getCount() > 0 && selected_item_position != NO_SELECTION) {
			Cursor c = (Cursor) property_list_adapter.getItem(selected_item_position);
			c.moveToFirst();
			return FavouritePropertiesDBHelper.getProperty(c);
		} else {
			return null;
		}
	}

	public ArrayList<Property> getPropertyList() {
		Log.d(TAG, "getPropertyList()");
		Cursor c = getActivity().getContentResolver().query(FavouritePropertiesProvider.CONTENT_URI, null, "", null, "");

		return FavouritePropertiesDBHelper.getProperties(c);
	}

	public void addProperty(Property property) {
		ContentValues values = FavouritePropertiesDBHelper.getContentValues(property);
		getActivity().getContentResolver().insert(FavouritePropertiesProvider.CONTENT_URI, values);
	}

	public void clear() {
		getActivity().getContentResolver().delete(FavouritePropertiesProvider.CONTENT_URI, null, null);
	}

	public boolean isFavourite(Property property) {
		Cursor c = getActivity().getContentResolver().query(
				ContentUris.withAppendedId(FavouritePropertiesProvider.CONTENT_URI, property.ListingID), null, "", null, "");

		return c.getCount() > 0;
	}

	public void removeProperty(Property property) {
		if (property != null) {
			getListView().setItemChecked(selected_item_position, false);
			selected_item_position = NO_SELECTION;
			getActivity().getContentResolver().delete(
					ContentUris.withAppendedId(FavouritePropertiesProvider.CONTENT_URI, property.ListingID), null, null);
		}
	}

	public Property updateProperty(Property property) {
		ContentValues values = new ContentValues();
		values.put("Body", property.Body);
		getActivity().getContentResolver().update(ContentUris.withAppendedId(FavouritePropertiesProvider.CONTENT_URI, property.ListingID),
				values, "", null);

		return getProperty(property.ListingID);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader()");

		return new CursorLoader(getActivity(), FavouritePropertiesProvider.CONTENT_URI, FAVOURITES_PROJECTION, "", null, "");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "onLoadFinished()");
		property_list_adapter.swapCursor(cursor);
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(TAG, "onLoaderReset()");
		property_list_adapter.swapCursor(null);
	}
}
