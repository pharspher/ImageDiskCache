import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageDiskCache
{
	private static final String TAG = "ImageDiskCache";

	private static final int MAX_CACHED_IMAGE = 15;
	public static final int IO_BUFFER_SIZE = 4096;

	private final File mCacheDir;

	public static ImageDiskCache getCache(Context context, String cachePath)
	{
		File cacheDir = context.getDir(getCacheDir(cachePath), Context.MODE_PRIVATE);

		if (!cacheDir.isDirectory() || !cacheDir.canWrite()) {
			Logger.d(TAG, "error!!");
			return null;
		}

		return new ImageDiskCache(cacheDir);
	}

	public Bitmap getCacheImage(String key)
	{
		File file = new File(mCacheDir, key);
		Logger.d(TAG, "check " + file.getPath());

		if (!file.exists()) {
			Logger.d(TAG, key + " miss");
			return null;
		}

		Logger.d(TAG, key + " hit");
		file.setLastModified(System.currentTimeMillis());

		return BitmapFactory.decodeFile(file.getPath());
	}

	public void cacheImage(String key, Bitmap bitmap)
	{
		Logger.d(TAG, "cache " + key);

		flushCache();

		File file = new File(mCacheDir, key);

		try {
			saveBitmapToFile(bitmap, file);
		} catch (FileNotFoundException e) {
			Logger.d(TAG, "cacheImage() error: " + e.getMessage());
		}
	}

	private void flushCache()
	{
		Logger.d(TAG, "flushCache()");
		File[] list = mCacheDir.listFiles();

		Logger.d(TAG, "current cache files: " + list.length + ", cache limit = " + MAX_CACHED_IMAGE);
		if (list.length < MAX_CACHED_IMAGE) {
			return;
		}

		Arrays.sort(list, new Comparator<File>()
		{
			public int compare(final File f1, final File f2)
			{
				return new Long(f2.lastModified()).compareTo(new Long(f1.lastModified()));
			}
		});

		for (int i = MAX_CACHED_IMAGE - 1; i < list.length; i++) {
			Logger.d(TAG, "delete " + list[i].getName());
			list[i].delete();
		}
	}

	private void saveBitmapToFile(Bitmap bitmap, File file) throws FileNotFoundException
	{
		FileOutputStream fOut = new FileOutputStream(file);
		OutputStream out = new BufferedOutputStream(fOut, IO_BUFFER_SIZE);

		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

		try {
			out.close();
		} catch (Exception e) {
			Logger.d(TAG, "saveBitmapToFile() error: " + e.getMessage());
		}
	}

	private ImageDiskCache(File cacheDir)
	{
		mCacheDir = cacheDir;
	}

	public static String getCacheDir(String path)
	{
		return new File(path).getPath();
	}
}
