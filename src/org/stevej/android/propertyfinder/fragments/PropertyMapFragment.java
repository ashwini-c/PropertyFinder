package org.stevej.android.propertyfinder.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.stevej.android.propertyfinder.R;
import org.stevej.android.propertyfinder.activities.PropertyFinderActivity;
import org.stevej.android.propertyfinder.model.Property;

import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PropertyMapFragment extends MapFragment {
	private static final String			TAG					= "PropertyMapFragment";
	private GoogleMap					map					= null;
	private LatLngBounds				map_bounds			= null;
	private HashMap<String, Property>	marker_to_property	= new HashMap<String, Property>();
	private HashMap<Integer, Marker>	property_to_marker	= new HashMap<Integer, Marker>();
	private Property					selected_property	= null;

	public PropertyMapFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
	}

	public void setPropertyList(ArrayList<Property> new_properties, Property new_selected_property) {
		Log.d(TAG, "setPropertyList() : " + new_properties.size());
		map.clear();
		property_to_marker.clear();
		marker_to_property.clear();

		selected_property = new_selected_property;

		addPropertyMarkers(new_properties);
	}

	public void updateSelection(Property property) {
		Log.d(TAG, "updateSelection() : " + property.Title);
		if (selected_property != null) {
			Marker current = property_to_marker.get(selected_property.ListingID);
			if (current != null) {
				current.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.property_icon));
			}
		}
		Marker new_selection = property_to_marker.get(property.ListingID);
		if (new_selection != null) {
			new_selection.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.selected_property_icon));
			LatLng lat_lng = new LatLng(property.Latitude, property.Longitude);
			map.animateCamera(CameraUpdateFactory.newLatLng(lat_lng));
			bounceMarker(new_selection);
		}
		selected_property = property;
	}

	private LatLngBounds calculateMapBounds(ArrayList<Property> properties) {
		LatLngBounds.Builder bounds_builder = new LatLngBounds.Builder();

		for (int i = 0; i < properties.size(); i++) {
			Property p = properties.get(i);

			if (!p.HasLocation) {
				continue;
			}

			LatLng lat_lng = new LatLng(p.Latitude, p.Longitude);
			bounds_builder.include(lat_lng);
		}
		try {
			LatLngBounds bounds = bounds_builder.build();
			return bounds;
		} catch (IllegalStateException e) {
			return null;
		}

	}

	private void addPropertyMarkers(final ArrayList<Property> properties) {
		Log.d(TAG, "addPropertyMarkers() : " + properties.size());

		map_bounds = calculateMapBounds(properties);

		Geocoder geocoder = new Geocoder(getActivity());

		for (int i = 0; i < properties.size(); i++) {
			Property p = properties.get(i);

			if (!p.HasLocation && !p.Address.equals("") && map_bounds != null) {
				try {
					List<Address> addresses = geocoder.getFromLocationName(p.Address, 5, map_bounds.southwest.latitude, map_bounds.southwest.longitude,
							map_bounds.northeast.latitude, map_bounds.northeast.longitude);

					if (addresses.size() > 0) {
						Address address = addresses.get(0);
						p.Latitude = address.getLatitude();
						p.Longitude = address.getLongitude();
					} else {
						continue;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			LatLng lat_lng = new LatLng(p.Latitude, p.Longitude);

			MarkerOptions mo = new MarkerOptions();
			mo.position(lat_lng);
			mo.title(p.Title);
			mo.snippet(p.PriceDisplay);
			mo.draggable(false);
			BitmapDescriptor bd;

			if (selected_property != null) {
				Log.d(TAG, "p id =  : " + p.ListingID + ", sel id = " + selected_property.ListingID);
			}
			if (selected_property != null && p.ListingID == selected_property.ListingID) {
				bd = BitmapDescriptorFactory.fromResource(R.drawable.selected_property_icon);
			} else {
				bd = BitmapDescriptorFactory.fromResource(R.drawable.property_icon);
			}
			mo.icon(bd);

			Marker marker = map.addMarker(mo);

			marker_to_property.put(marker.getId(), p);
			property_to_marker.put(p.ListingID, marker);
		}
		if (map_bounds != null) {
			moveCameraToBounds(map_bounds);
		}

	}

	private void bounceMarker(final Marker marker) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		final long duration = 1500;

		final Interpolator interpolator = new BounceInterpolator();

		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
				marker.setAnchor(0.5f, 1.0f + t);

				if (t > 0.0) {
					// Post again 16ms later (60fps)
					handler.postDelayed(this, 16);
				}
			}
		});
	}

	private void moveCameraToBounds(final LatLngBounds bounds) {
		Log.d(TAG, "moveCameraToBounds()");
		final View map_view = getView();
		if (map_view.getViewTreeObserver().isAlive()) {
			map_view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					Log.d(TAG, "onGlobalLayout()");
					map_view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
				}
			});
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated()");
		super.onActivityCreated(savedInstanceState);

		if (map == null) {
			map = getMap();
		}
		if (map != null) {
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			map.setMyLocationEnabled(true);
			map.getUiSettings().setMyLocationButtonEnabled(true);

			map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
			Log.d(TAG, "setUpMap() : set info window adapter");
		}
	}

	class CustomInfoWindowAdapter implements InfoWindowAdapter {
		private final View	info_window;

		CustomInfoWindowAdapter() {
			info_window = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
		}

		@Override
		public View getInfoWindow(final Marker marker) {
			Property p = marker_to_property.get(marker.getId());

			final ImageView thumbnail = (ImageView) info_window.findViewById(R.id.badge);
			final TextView title_view = ((TextView) info_window.findViewById(R.id.title));
			final TextView snippet_view = ((TextView) info_window.findViewById(R.id.snippet));

			Log.d(TAG, "getInfoWindow : " + marker.getTitle() + ", " + p.Title + ", " + p.ThumbURL);

			String title = marker.getTitle();
			if (title != null) {
				SpannableString titleText = new SpannableString(title);
				title_view.setText(titleText);
			} else {
				title_view.setText("");
			}

			String snippet = marker.getSnippet();
			if (snippet != null) {
				SpannableString snippetText = new SpannableString(snippet);
				snippet_view.setText(snippetText);
			} else {
				snippet_view.setText("");
			}

			((PropertyFinderActivity) getActivity()).getImageLoader().get(p.ThumbURL, new ImageListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
				}

				@Override
				public void onResponse(ImageContainer container, boolean is_immediate) {
					Bitmap bitmap = container.getBitmap();
					if (bitmap == null) {
						return;
					}
					thumbnail.setImageBitmap(bitmap);
					if (marker.isInfoWindowShown()) {
						marker.hideInfoWindow();
						marker.showInfoWindow();
					}
				}
			});
			return info_window;
		}

		@Override
		public View getInfoContents(Marker marker) {
			return null;
		}
	}

}
