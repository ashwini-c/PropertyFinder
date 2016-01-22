package org.stevej.android.propertyfinder.model;

import java.util.ArrayList;
import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Property implements Parcelable {
	private static final String	TAG					= "Property";
	public int					ListingID;
	public String				Title;
	public String				Category;
	public String				PriceDisplay;
	public int					PriceNumeric;
	public String				ThumbURL;

	public boolean				HasGallery;

	public boolean				HasLocation;
	public boolean				HasAgency;
	public double				Latitude;
	public double				Longitude;
	public String				Address;
	public String				PropertyType;
	public String				Agency_name;
	public String				Agency_phone;

	public String				Body;
	public ArrayList<String>	photo_thumb_urls	= new ArrayList<String>();
	public ArrayList<String>	photo_large_urls	= new ArrayList<String>();

	public Property() {

	}

	public String toJSONString() {
		return toJSONObject().toString();
	}

	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();
		try {
			json.put("ListingId", ListingID);
			json.put("Title", Title.replace("&", "%26"));
			json.put("PriceDisplay", PriceDisplay);
			json.put("PriceNumeric", PriceNumeric);
			json.put("PictureHref", ThumbURL);

			json.put("HasGallery", HasGallery);
			json.put("Address", Address);
			json.put("Category", Category);
			json.put("PropertyType", PropertyType);

			json.put("HasLocation", HasLocation);

			if (HasLocation) {
				JSONObject locn = new JSONObject();
				locn.put("Latitude", Latitude);
				locn.put("Longitude", Longitude);
				json.put("GeographicLocation", locn);
			}

			json.put("HasAgency", HasAgency);
			if (HasAgency) {
				JSONObject agency = new JSONObject();
				agency.put("Name", Agency_name.replace("&", "%26"));
				agency.put("PhoneNumber", Agency_phone);
				json.put("Agency", agency);
			}
			return json;
		} catch (JSONException e) {

			e.printStackTrace();
		}
		return null;
	}

	private Property(Parcel in) {
		Log.d(TAG, "Property(Parcel in)");

		ListingID = in.readInt();
		Title = in.readString();
		Category = in.readString();
		PriceDisplay = in.readString();
		PriceNumeric = in.readInt();
		ThumbURL = in.readString();

		HasGallery = in.readByte() == 1;
		HasLocation = in.readByte() == 1;
		HasAgency = in.readByte() == 1;

		Latitude = in.readDouble();
		Longitude = in.readDouble();

		Address = in.readString();
		PropertyType = in.readString();
		Agency_name = in.readString();
		Agency_phone = in.readString();
		Body = in.readString();
		Log.d(TAG, "Property(Parcel in) done");
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		Log.d(TAG, "writeToParcel(Parcel out)");

		out.writeInt(ListingID);
		out.writeString(Title);
		out.writeString(Category);
		out.writeString(PriceDisplay);
		out.writeInt(PriceNumeric);
		out.writeString(ThumbURL);

		out.writeByte((byte) (HasGallery ? 1 : 0));
		out.writeByte((byte) (HasLocation ? 1 : 0));
		out.writeByte((byte) (HasAgency ? 1 : 0));

		out.writeDouble(Latitude);
		out.writeDouble(Longitude);

		out.writeString(Address);
		out.writeString(PropertyType);
		out.writeString(Agency_name);
		out.writeString(Agency_phone);
		out.writeString(Body);
		Log.d(TAG, "writeToParcel(Parcel out) done");
	}

	public static final Parcelable.Creator<Property>	CREATOR					= new Parcelable.Creator<Property>() {
																					public Property createFromParcel(Parcel in) {
																						//Log.d(TAG,"createFromParcel()");
																						Property p = new Property(in);
																						Log.d(TAG,"createFromParcel() : " + p.toJSONString());
																						
																						return p;//new Property(in);
																					}

																					public Property[] newArray(int size) {
																						return new Property[size];
																					}
																				};
	public static Comparator<Property>					COMPARE_BY_TITLE		= new Comparator<Property>() {
																					public int compare(Property one, Property other) {
																						return one.Title.compareTo(other.Title);
																					}
																				};
	public static Comparator<Property>					COMPARE_BY_PRICE_ASC	= new Comparator<Property>() {
																					public int compare(Property one, Property other) {
																						return one.PriceNumeric - other.PriceNumeric;
																					}
																				};
	public static Comparator<Property>					COMPARE_BY_PRICE_DESC	= new Comparator<Property>() {
																					public int compare(Property one, Property other) {
																						return other.PriceNumeric - one.PriceNumeric;
																					}
																				};

}
