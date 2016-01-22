package org.stevej.android.propertyfinder.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.util.Log;

public class TemporaryFileUtils {
	private static final String	TAG	= "TemporaryFileUtils";

	public static Uri createTempBitmapFile(Context context, String url, BitmapCache bitmap_cache) {
		String file_name = url.substring(url.lastIndexOf("/"));
		String file_name_base = file_name.substring(0, file_name.lastIndexOf('.'));
		String file_name_ext = file_name.substring(file_name.lastIndexOf('.') + 1);

		File cache_dir = context.getExternalCacheDir();
		File tmp_file;
		try {
			tmp_file = File.createTempFile(file_name_base, "." + file_name_ext, cache_dir);
			Bitmap bitmap = bitmap_cache.get(url);
			if (bitmap == null){
				return null;
			}
			FileOutputStream fos = new FileOutputStream(tmp_file);
			bitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
			return Uri.fromFile(tmp_file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void deleteTempBitmapFile(Uri uri) {
		if (uri == null) {
			return;
		}
		File bitmap_file = new File(uri.getPath());
		boolean result = bitmap_file.delete();

		Log.d(TAG, "deleteTempBitmapFile : " + bitmap_file.getAbsolutePath() + ", " + result);
	}
}
