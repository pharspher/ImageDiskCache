import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class CachedRadioIcon
{
	private static final String TAG = "CachedRadioIcon";

	private static final String CACHE_DIR = "radio_icon_cache";
	private static final int IO_BUFFER_SIZE = ImageDiskCache.IO_BUFFER_SIZE;

	public interface OnIconLoadedListener
	{
		public void onIconLoaded();
	};

	private ImageDiskCache mCache;
	private Bitmap mIcon;

	public CachedRadioIcon(Context context)
	{
		mCache = ImageDiskCache.getCache(context, CACHE_DIR);
	}

	public void loadIcon(final RadioInfo info, final OnIconLoadedListener listener)
	{
		final String iconName = info.getID();

		Logger.d(TAG, "loadIcon() " + iconName);

		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params)
			{
				Logger.d(TAG, "check " + iconName);

				mIcon = mCache.getCacheImage(iconName);

				if (mIcon != null) {
					Logger.d(TAG, iconName + " hit");
					return null;
				}

				Logger.d(TAG, iconName + " miss");
				mIcon = getIconFromUrl(info.getRadioImgPath());
				mCache.cacheImage(info.getID(), mIcon);

				return null;
			}

			@Override
			protected void onPostExecute(Void result)
			{
				listener.onIconLoaded();
				super.onPostExecute(result);
			}
		}.execute();
	}

	public void loadIcon(final String id, final String url, final OnIconLoadedListener listener)
	{
		Logger.d(TAG, "loadIcon() " + id);

		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params)
			{
				Logger.d(TAG, "check " + id);

				mIcon = mCache.getCacheImage(id);

				if (mIcon != null) {
					Logger.d(TAG, id + " hit");
					return null;
				}

				Logger.d(TAG, id + " miss");
				mIcon = getIconFromUrl(url);
				mCache.cacheImage(id, mIcon);

				return null;
			}

			@Override
			protected void onPostExecute(Void result)
			{
				listener.onIconLoaded();
				super.onPostExecute(result);
			}
		}.execute();

	}

	private Bitmap getIconFromUrl(String url)
	{
		Logger.d(TAG, "getIconFromUrlImpl(): " + url);

		InputStream urlStream = null;

		try {
			urlStream = new URL(url).openStream();
		} catch (Exception e) {
			Logger.d(TAG, "getIconFromUrl openStream() exception: " + e.getMessage());
			return null;
		}

		InputStream inStream = new BufferedInputStream(urlStream, IO_BUFFER_SIZE);
		Bitmap bitmap = BitmapFactory.decodeStream(inStream);

		try {
			inStream.close();
		} catch (Exception e) {
			Logger.d(TAG, "getIconFromUrl close() exception: " + e.getMessage());
		}

		return bitmap;
	}

	public Bitmap getIcon()
	{
		return mIcon;
	}
}
