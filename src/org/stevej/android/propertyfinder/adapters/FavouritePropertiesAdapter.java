package org.stevej.android.propertyfinder.adapters;

import org.stevej.android.propertyfinder.R;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class FavouritePropertiesAdapter extends CursorAdapter {
	private static final String	TAG				= "FavouritePropertiesAdapter";
	private ImageLoader			image_loader	= null;

	public FavouritePropertiesAdapter(Context context, ImageLoader image_loader) {
		super(context, null, 0);
		Log.d(TAG, "FavouritePropertiesAdapter()");
		this.image_loader = image_loader;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		//Log.d(TAG, "bindView()");
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.title.setText(cursor.getString((Integer) holder.title.getTag()));
		holder.price.setText(cursor.getString((Integer) holder.price.getTag()));
		holder.thumbnail.setImageUrl(cursor.getString((Integer) holder.thumbnail.getTag()), image_loader);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		//Log.d(TAG, "newView()");

		View view = LayoutInflater.from(context).inflate(R.layout.property_list_item, null);

		ViewHolder holder = new ViewHolder();

		// use the Volley library's NetworkImageView
		holder.thumbnail = (NetworkImageView) view.findViewById(R.id.property_thumbnail);
		holder.title = (TextView) view.findViewById(R.id.property_title);
		holder.price = (TextView) view.findViewById(R.id.property_price);

		holder.title.setTag(cursor.getColumnIndexOrThrow("Title"));
		holder.price.setTag(cursor.getColumnIndexOrThrow("PriceDisplay"));
		holder.thumbnail.setTag(cursor.getColumnIndexOrThrow("PictureHref"));

		view.setTag(holder);
		return view;
	}

	static class ViewHolder {
		NetworkImageView	thumbnail;
		TextView			title;
		TextView			price;
	}
}
