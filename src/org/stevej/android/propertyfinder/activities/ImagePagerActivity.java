package org.stevej.android.propertyfinder.activities;

import java.util.ArrayList;

import org.stevej.android.propertyfinder.R;
import org.stevej.android.propertyfinder.fragments.ImagePageFragment;
import org.stevej.android.propertyfinder.utils.BitmapCache;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager.LayoutParams;
import android.widget.GridView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.viewpagerindicator.CirclePageIndicator;

public class ImagePagerActivity extends Activity {
	private static final String	TAG				= "ImagePagerActivity";
	private ArrayList<String>	image_urls;
	private int position;
	private ViewPager			pager;
	private ImagePagerAdapter	image_pager_adapter;

	private RequestQueue		request_queue	= null;
	private ImageLoader			image_loader	= null;
	private BitmapCache				bitmap_cache		= null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		setContentView(R.layout.image_pager);

		// Set up activity to go full screen
		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

		// TODO : create Volley request queue and image loader
		request_queue = Volley.newRequestQueue(getApplicationContext());
		bitmap_cache = new BitmapCache();
		image_loader = new ImageLoader(request_queue, bitmap_cache);

		// TODO : get list of image urls and initial photo position from the Intent that launched this activity
		image_urls = getIntent().getStringArrayListExtra("largeURL");
		position = getIntent().getIntExtra("position", -1);

		// TODO : create an ImagePagerAdapter instance

		image_pager_adapter = new ImagePagerAdapter(getFragmentManager());

		pager = (ViewPager) findViewById(R.id.image_pager);

		// TODO : configure the ViewPager including setting its adapter
		pager.setAdapter(image_pager_adapter);

		// associate the indicator with the ViewPager
		CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.image_pager_indicator);
		indicator.setViewPager(pager);

		// TODO : configure action bar as appropriate

	}

	public ImageLoader getImageLoader() {
		return image_loader;
	}

	private class ImagePagerAdapter extends FragmentStatePagerAdapter {


		public ImagePagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub

		}

		@Override
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub

			Fragment imageFragment;

			imageFragment = ImagePageFragment.newInstance(image_urls.get(position), image_loader);
			return imageFragment;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return image_urls.size();
		}

		// TODO : implement constructor and required methods getItem and getCount

		// getItem will return a new fragment that displays an image

	}

}
