package org.stevej.android.propertyfinder.fragments;

import org.stevej.android.propertyfinder.R;
import org.stevej.android.propertyfinder.model.Property;

import com.android.volley.toolbox.ImageLoader.ImageCache;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class PropertyDetailsFragment extends Fragment {
	private static final String	TAG			= "PropertyDetailsFragment";

	private Property			property	= null;

	// holds the top level view for this fragment's layout
	View						details_view;
	String info = "INFO";
	String photo = "PHOTO";
	TabHost tab_host;
	TabSpec infoSpec,photoSpec;
	ImageGridFragment imgGrid;
	public PropertyDetailsFragment() {
	}

	// called when the fragment's UI is being constructed. Initialise with current property details
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView()");
		details_view = inflater.inflate(R.layout.property_details, container, false);
		tab_host = (TabHost) details_view.findViewById(R.id.tab_host);
		tab_host.setup();
		infoSpec = tab_host.newTabSpec(info);
		infoSpec.setIndicator(info);
		infoSpec.setContent(R.id.info_tab);
		photoSpec = tab_host.newTabSpec(photo);
		photoSpec.setIndicator(photo);
		photoSpec.setContent(R.id.photos_tab);
		tab_host.addTab(infoSpec);
		tab_host.addTab(photoSpec);
		tab_host.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub

			}
		});
		imgGrid = (ImageGridFragment) getFragmentManager().findFragmentById(R.id.image_grid_fragment);

		// TODO : configure and add the tabs ('Info' and 'Photos')

		if (property != null) {

			TextView title_view = (TextView) details_view.findViewById(R.id.property_details_title);
			TextView price_view = (TextView) details_view.findViewById(R.id.property_details_price);
			TextView type_view = (TextView) details_view.findViewById(R.id.property_details_type);
			TextView address_view = (TextView) details_view.findViewById(R.id.property_details_address);
			TextView agency_name_view = (TextView) details_view.findViewById(R.id.property_details_agency_name);
			TextView agency_phone_view = (TextView) details_view.findViewById(R.id.property_details_agency_phone);
			TextView body_view = (TextView) details_view.findViewById(R.id.property_details_body);

			title_view.setText(property.Title);
			price_view.setText(property.PriceDisplay);
			type_view.setText(property.PropertyType);
			address_view.setText(property.Address);
			agency_name_view.setText(property.Agency_name);
			agency_phone_view.setText(property.Agency_phone);
			body_view.setText(property.Body);
		}
		return details_view;
	}

	public void update(Property property) {
		Log.d(TAG, "setContent()");

		this.property = property;
		TextView title_view = (TextView) details_view.findViewById(R.id.property_details_title);
		TextView price_view = (TextView) details_view.findViewById(R.id.property_details_price);
		TextView type_view = (TextView) details_view.findViewById(R.id.property_details_type);
		TextView address_view = (TextView) details_view.findViewById(R.id.property_details_address);
		TextView agency_name_view = (TextView) details_view.findViewById(R.id.property_details_agency_name);
		TextView agency_phone_view = (TextView) details_view.findViewById(R.id.property_details_agency_phone);
		TextView body_view = (TextView) details_view.findViewById(R.id.property_details_body);

		title_view.setText(property.Title);
		price_view.setText(property.PriceDisplay);
		type_view.setText(property.PropertyType);
		address_view.setText(property.Address);
		agency_name_view.setText(property.Agency_name);
		agency_phone_view.setText(property.Agency_phone);
		body_view.setText(property.Body);

		// TODO : set the ImageGridFragment's thumb and photo URLs
		imgGrid.setImageUrls(property.photo_thumb_urls, property.photo_large_urls);
	}

	public void clear() {
		Log.d(TAG, "clear()");

		TextView title_view = (TextView) details_view.findViewById(R.id.property_details_title);
		TextView price_view = (TextView) details_view.findViewById(R.id.property_details_price);
		TextView type_view = (TextView) details_view.findViewById(R.id.property_details_type);
		TextView address_view = (TextView) details_view.findViewById(R.id.property_details_address);
		TextView agency_name_view = (TextView) details_view.findViewById(R.id.property_details_agency_name);
		TextView agency_phone_view = (TextView) details_view.findViewById(R.id.property_details_agency_phone);
		TextView body_view = (TextView) details_view.findViewById(R.id.property_details_body);

		title_view.setText("");
		price_view.setText("");
		type_view.setText("");
		address_view.setText("");
		agency_name_view.setText("");
		agency_phone_view.setText("");
		body_view.setText("");

		// TODO : clear the ImageGridFragment
		imgGrid.clear();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
	}

}
