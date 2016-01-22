package org.stevej.android.propertyfinder.fragments;

import org.stevej.android.propertyfinder.R;
import org.stevej.android.propertyfinder.activities.ImagePagerActivity;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

/**
 * This fragment will populate a child of the ViewPager from {@link ImagePagerActivity}.
 */
public class ImagePageFragment extends Fragment {
	private static final String	TAG					= "ImagePageFragment";
	private static final String	EXTRA_IMAGE_DATA	= "extra_image_data";
	private  String				image_url;
	private NetworkImageView	image_view;
	static ImageLoader  imageLoader;

	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public ImagePageFragment() {
	}

	/**
	 * Factory method to generate a new instance of the fragment given an image URL.
	 * 
	 * @param image_url
	 *            The image url to load
	 * @return A new instance of ImagePageFragment with imageNum extras
	 */
	public static ImagePageFragment newInstance(String img_url,ImageLoader imgLoader) {

		// TODO : create a new ImagePageFragment, provide it with the image URL in its arguments, return the fragment
		ImagePageFragment imageFragment = new ImagePageFragment();
		Bundle bundle = new Bundle();
		bundle.putString("imgurl", img_url);
		imageFragment.setArguments(bundle);
		imageLoader = imgLoader;

		return imageFragment ;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO : get the image URL from the fragment's arguments
		image_url = getArguments().getString("imgurl");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


		// TODO : inflate the layout for an image page, get hold of the NetworkImageView item, set its URL
		View view;
		NetworkImageView image_view;
		view = inflater.inflate(R.layout.image_page, null);
		image_view = (NetworkImageView) view.findViewById(R.id.image_item);
		image_view.setImageUrl(image_url, imageLoader);
		return view;
	}

}
