/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.stevej.android.propertyfinder.fragments;

import java.util.ArrayList;

import org.stevej.android.propertyfinder.R;
import org.stevej.android.propertyfinder.activities.ImagePagerActivity;
import org.stevej.android.propertyfinder.activities.PropertyFinderActivity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class ImageGridFragment extends Fragment implements OnItemClickListener {
	private static final String	TAG				= "ImageGridFragment";
	private ImageGridAdapter	image_grid_adapter;
	private ArrayList<String>	thumbnail_urls	= new ArrayList<String>();
	private ArrayList<String>	photo_urls		= new ArrayList<String>();


	public ImageGridFragment() {

	}

	public void setImageUrls(ArrayList<String> thumb_urls, ArrayList<String> photo_urls) {

		// TODO : receive a list of thumbnail URLs and corresponding photo URLs, update URL array lists
		// TODO : notify the adapter that the displayed data has changed

		thumbnail_urls = thumb_urls;
		this.photo_urls = photo_urls;
		image_grid_adapter.notifyDataSetChanged();
	}

	public void clear() {
		// TODO : clear any URL data and notify the adapter
		thumbnail_urls.clear();
		photo_urls.clear();
		image_grid_adapter.notifyDataSetChanged();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated()");
		super.onActivityCreated(savedInstanceState);

		// TODO : create the image grid adapter
		image_grid_adapter = new ImageGridAdapter(getActivity(), ((PropertyFinderActivity) getActivity()).getImageLoader());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView()");

		// TODO : inflate the image grid layout file, find the GridView item and set its click listener

		View view = inflater.inflate(R.layout.image_grid,
				container, false);
		return view;
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		GridView grid_view = (GridView) getView();
		grid_view.setAdapter(image_grid_adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		// TODO : handle a click on a thumbnail. Launch the ImagePagerActivity providing it with the list of photo URLs
		// to display and the position of the clicked item
		Intent intent = new Intent(getActivity(), ImagePagerActivity.class);
		intent.putStringArrayListExtra("largeURL", photo_urls);
		intent.putExtra("position", position);
		startActivity(intent);
	}

	private class ImageGridAdapter extends BaseAdapter {
		private static final String		TAG				= "ImageGridAdapter";
		private ImageLoader				image_loader	= null;
		private Context					context			= null;
		private GridView.LayoutParams	image_view_layout_params;
		private LayoutInflater	layout_inflater;

		public ImageGridAdapter(Context context, ImageLoader image_loader) {
			super();
			layout_inflater =  LayoutInflater.from(context); 
			this.image_loader = image_loader;
			this.context = context;

			int image_width = (int) context.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_width);
			int image_height = (int) context.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_height);
			image_view_layout_params = new GridView.LayoutParams(image_width, image_height);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "getView(" + position + ")");

			NetworkImageView image_view;

			if(convertView == null)
			{

				convertView = layout_inflater.inflate(R.layout.image_grid_item, null);
				// use the Volley library's NetworkImageView
				image_view = (NetworkImageView) convertView.findViewById(R.id.grid_item);

			}
			else
			{
				return (NetworkImageView)convertView;
			}
			image_view.setImageUrl(thumbnail_urls.get(position), image_loader);
			convertView.setLayoutParams(image_view_layout_params);
			return convertView;
			// TODO : if convertView == null create a new NetworkImageView and configure it, otherwise use convertView (cast to NetworkImageView)

			// TODO : get the thumbnail URL for this position and set it as the NetworkImageView's URL

		}

		@Override
		public int getCount() {
			return thumbnail_urls.size();
		}

		@Override
		public String getItem(int position) {
			return thumbnail_urls.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}



	}

}
