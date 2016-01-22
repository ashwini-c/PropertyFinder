package org.stevej.android.propertyfinder.database;

import java.util.ArrayList;

import org.stevej.android.propertyfinder.model.Property;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FavouritePropertiesDBHelper extends SQLiteOpenHelper {
	private static final String					TAG						= "FavouritePropertiesDBHelper";
	private static final String					DATABASE_NAME			= "FavouriteProperties.db";
	private static final String					FAVOURITES_TABLE		= "FavouriteProperties";
	private static final int					SCHEMA_VERSION			= 1;

	private static final String					CREATE_FAVOURITES_TABLE	= "CREATE TABLE " + FAVOURITES_TABLE + " (ListingId INTEGER PRIMARY KEY,"
																				+ "Title TEXT," + "Category TEXT," + "PriceDisplay TEXT,"
																				+ "PriceNumeric INTEGER," + "PictureHref TEXT," + "HasGallery INTEGER,"
																				+ "HasLocation INTEGER," + "HasAgency INTEGER," + "Latitude REAL,"
																				+ "Longitude REAL," + "Address TEXT," + "PropertyType TEXT,"
																				+ "AgencyName TEXT," + "AgencyPhone TEXT," + "Body TEXT," + "RegionId TEXT,"
																				+ "DistrictId TEXT," + "SuburbId TEXT," + "LocationType INTEGER,"
																				+ "HasDetails INTEGER);";

	private static final String					DROP_FAVOURITES_TABLE	= "DROP TABLE IF EXISTS " + FAVOURITES_TABLE + ";";

	private static FavouritePropertiesDBHelper	singleton				= null;

	synchronized public static FavouritePropertiesDBHelper getInstance(Context context) {
		Log.d(TAG, "FavouritePropertiesDBHelper.getInstance()");
		if (singleton == null) {
			singleton = new FavouritePropertiesDBHelper(context);
		}
		return (singleton);
	}

	private FavouritePropertiesDBHelper(Context ctxt) {
		super(ctxt, DATABASE_NAME, null, SCHEMA_VERSION);

		Log.d(TAG, "FavouritePropertiesDBHelper()");
	}

	public static ContentValues getContentValues(Property property) {
		ContentValues values = new ContentValues();
		values.put("ListingId", property.ListingID);
		values.put("Title", property.Title);
		values.put("Category", property.Category);
		values.put("PriceDisplay", property.PriceDisplay);
		values.put("PriceNumeric", property.PriceNumeric);
		values.put("PictureHref", property.ThumbURL);
		values.put("HasGallery", property.HasGallery ? 1 : 0);
		values.put("HasLocation", property.HasLocation ? 1 : 0);
		values.put("HasAgency", property.HasAgency ? 1 : 0);
		values.put("Latitude", property.Latitude);
		values.put("Longitude", property.Longitude);
		values.put("Address", property.Address);
		values.put("PropertyType", property.PropertyType);
		values.put("AgencyName", property.Agency_name);
		values.put("AgencyPhone", property.Agency_phone);
		values.put("Body", property.Body);

		return values;
	}

	public static Property getProperty(Cursor cursor) {
		Property property = new Property();

		property.Title = cursor.getString(cursor.getColumnIndexOrThrow("Title"));
		property.ListingID = cursor.getInt(cursor.getColumnIndexOrThrow("ListingId"));
		property.Category = cursor.getString(cursor.getColumnIndexOrThrow("Category"));
		property.PriceDisplay = cursor.getString(cursor.getColumnIndexOrThrow("PriceDisplay"));
		property.PriceNumeric = cursor.getInt(cursor.getColumnIndexOrThrow("PriceNumeric"));
		property.ThumbURL = cursor.getString(cursor.getColumnIndexOrThrow("PictureHref"));
		property.HasGallery = cursor.getInt(cursor.getColumnIndexOrThrow("HasGallery")) == 1;
		property.HasLocation = cursor.getInt(cursor.getColumnIndexOrThrow("HasLocation")) == 1;
		property.HasAgency = cursor.getInt(cursor.getColumnIndexOrThrow("HasAgency")) == 1;
		property.Latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("Latitude"));
		property.Longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("Longitude"));
		property.Address = cursor.getString(cursor.getColumnIndexOrThrow("Address"));
		property.PropertyType = cursor.getString(cursor.getColumnIndexOrThrow("PropertyType"));
		property.Agency_name = cursor.getString(cursor.getColumnIndexOrThrow("AgencyName"));
		property.Agency_phone = cursor.getString(cursor.getColumnIndexOrThrow("AgencyPhone"));
		property.Body = cursor.getString(cursor.getColumnIndexOrThrow("Body"));

		return property;
	}

	public static ArrayList<Property> getProperties(Cursor cursor) {
		cursor.moveToFirst();
		ArrayList<Property> properties = new ArrayList<Property>();

		while (!cursor.isAfterLast()) {
			properties.add(getProperty(cursor));
			cursor.moveToNext();
		}
		return properties;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate()");
		try {
			db.beginTransaction();
			db.execSQL(CREATE_FAVOURITES_TABLE);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "updgrading from " + oldVersion + " to " + newVersion);
		try {
			db.beginTransaction();
			db.execSQL(DROP_FAVOURITES_TABLE);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		Log.d(TAG, "downgrading from " + oldVersion + " to " + newVersion);
		try {
			db.beginTransaction();
			db.execSQL(DROP_FAVOURITES_TABLE);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		onCreate(db);
	}
}
