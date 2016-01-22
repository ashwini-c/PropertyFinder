package org.stevej.android.propertyfinder.providers;

import org.stevej.android.propertyfinder.database.FavouritePropertiesDBHelper;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class FavouritePropertiesProvider extends ContentProvider {
	private static final String	TAG					= "FavouritePropertiesProvider";

	private static final int	FAVOURITE			= 0;
	private static final int	FAVOURITES_LIST		= 1;
	private static final String	AUTHORITY			= "org.stevej.android.propertyfinder.providers.FavouritePropertiesProvider";

	public static final String	CONTENT_URI_STRING	= "content://" + AUTHORITY + "/favourites";

	public static final Uri		CONTENT_URI			= Uri.parse(CONTENT_URI_STRING);

	private UriMatcher			uri_matcher			= null;

	@Override
	public boolean onCreate() {
		Log.d(TAG, "onCreate()");

		uri_matcher = new UriMatcher(UriMatcher.NO_MATCH);
		uri_matcher.addURI(AUTHORITY, "favourites/#", FAVOURITE);
		uri_matcher.addURI(AUTHORITY, "favourites", FAVOURITES_LIST);

		return FavouritePropertiesDBHelper.getInstance(getContext()) != null;
	}

	private Cursor getFavourite(String id) {
		Log.d(TAG, "getFavourite()");
		String sql = "SELECT ListingId AS " + BaseColumns._ID + ",* FROM FavouriteProperties WHERE ListingId='" + id + "'";
		Cursor cursor = FavouritePropertiesDBHelper.getInstance(getContext()).getReadableDatabase().rawQuery(sql, null);
		return cursor;
	}

	private Cursor getFavourites() {
		Log.d(TAG, "getFavourites()");
		String sql = "SELECT ListingId AS " + BaseColumns._ID + ",* FROM FavouriteProperties";
		Cursor cursor = FavouritePropertiesDBHelper.getInstance(getContext()).getReadableDatabase().rawQuery(sql, null);
		cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
		return cursor;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "          uri = " + uri.toString());
		Log.d(TAG, "        limit = " + uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT));
		Log.d(TAG, "last path seg = " + uri.getLastPathSegment());
		if (selectionArgs != null) {
			for (int i = 0; i < selectionArgs.length; i++) {
				Log.d(TAG, "       arg[" + i + "] = " + selectionArgs[i]);
			}
		}

		switch (uri_matcher.match(uri)) {
			case FAVOURITES_LIST:
				return getFavourites();
			case FAVOURITE:
				return getFavourite(uri.getLastPathSegment());
			default:
				return null;
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int num_rows = 0;
		SQLiteDatabase db = FavouritePropertiesDBHelper.getInstance(getContext()).getWritableDatabase();
		switch (uri_matcher.match(uri)) {
			case FAVOURITES_LIST:
				num_rows = db.delete("FavouriteProperties", "1", null);
				getContext().getContentResolver().notifyChange(uri, null);
				break;
			case FAVOURITE:
				String id = uri.getLastPathSegment();
				num_rows = db.delete("FavouriteProperties", "ListingId = ?", new String[] { id });
				getContext().getContentResolver().notifyChange(uri, null);
				break;
		}
		return num_rows;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert()");
		SQLiteDatabase db = FavouritePropertiesDBHelper.getInstance(getContext()).getWritableDatabase();
		long id = db.insert("FavouriteProperties", null, values);

		getContext().getContentResolver().notifyChange(uri, null);
		return ContentUris.withAppendedId(CONTENT_URI, id);
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		Log.d(TAG, "update()");
		SQLiteDatabase db = FavouritePropertiesDBHelper.getInstance(getContext()).getWritableDatabase();
		String id = uri.getLastPathSegment();
		int num_rows = db.update("FavouriteProperties", values, "ListingId = ?", new String[] { id });
		getContext().getContentResolver().notifyChange(uri, null);

		Cursor c = getFavourite(uri.getLastPathSegment());
		StringBuilder sb = new StringBuilder();
		DatabaseUtils.dumpCursor(c, sb);
		Log.d(TAG, sb.toString());

		return num_rows;
	}
}
