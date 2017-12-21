package uk.co.sundroid.util.astro.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.DisplayMetrics;

public class MoonPhaseImage {
	
	public static final int SIZE_SMALL = 0;
	public static final int SIZE_MEDIUM = 1;
	public static final int SIZE_LARGE = 2;

	public static Bitmap makeImage(Resources resources, int drawable, double phase, boolean southernHemisphere, int size) throws Exception {
		DisplayMetrics metrics = resources.getDisplayMetrics();
		int densityDpi = metrics.densityDpi;
		Bitmap source = BitmapFactory.decodeResource(resources, drawable);

		int targetSize = size == SIZE_SMALL ? 31 : size == SIZE_MEDIUM ? 61 : 121;
		if (densityDpi > 160) {
			targetSize = size == SIZE_SMALL ? 45 : size == SIZE_MEDIUM ? 91 : 181;
		} else if (densityDpi < 160) {
			targetSize = size == SIZE_SMALL ? 23 : size == SIZE_MEDIUM ? 45 : 91;
		}

		Matrix matrix = new Matrix();
		matrix.setRectToRect(
			new RectF(0, 0, source.getWidth(), source.getHeight()),
			new RectF(0, 0, targetSize, targetSize),
			Matrix.ScaleToFit.FILL);
		matrix.postTranslate(0, 0);
		
		if (southernHemisphere) {
			matrix.postRotate(180);
			phase = 1 - phase;
		}
		
		int radius = (targetSize)/2;
		radius++;
		
		Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

		int[][] leftEdge = new int[radius + 1][14];
		int[][] rightEdge = new int[radius + 1][14];
		
		int position = 0;
		for (double p = phase - 0.02; p <= phase + 0.02 && position < 14; p += 0.003) {
			double pa = p < 0 ? p + 1d : p;
			pa = pa > 1 ? pa - 1 : p;
			if (pa < 0.02 || pa > 0.98 || (Math.abs(phase - pa) > 0.1)) {
				for (int r = 0; r <= radius; r++) {
					leftEdge[r][position] = 0;
					rightEdge[r][position] = 0;
				}
			} else {
				double s = Math.cos(2.0 * Math.PI * pa);
				for (int r = 0; r <= radius; r++) {
					int limb = (int)(s * radius * Math.cos(Math.asin(((double)r)/radius)));
					leftEdge[r][position] = pa <= 0.5 ? radius + limb : 0;
					rightEdge[r][position] = pa <= 0.5 ? targetSize : radius - limb;
				}
			}
			position++;
		}
		for (int y = 0; y < targetSize; y++) {
			int	r = Math.abs((y + 1) - radius);
			if (r < radius) {
				for (int x = 0; x < targetSize; x++) {
					double brightness = 0;
					int[] left = leftEdge[r];
					int[] right = rightEdge[r];
					for (int p = 0; p < left.length; p++) {
						if (x >= left[p] && x <= right[p]) {
							brightness += 1;
						}
					}
					brightness = brightness / (double)left.length;
					brightness = (brightness * 0.75) + 0.25;
					if (phase < 0.01 || phase > 0.99) {
						brightness = 0.25;
					}

					if (brightness < 1) {
						int sourcePixelColor = bitmap.getPixel(x, y);
						if (Color.alpha(sourcePixelColor) > 0) {
							int red = (int)(Color.red(sourcePixelColor) * brightness);
							int green = (int)(Color.green(sourcePixelColor) * brightness);
							int blue = (int)(Color.blue(sourcePixelColor) * brightness);
							bitmap.setPixel(x, y, Color.argb(Color.alpha(sourcePixelColor), red, green, blue));
						}
					}
				}
			}
		}
		return bitmap;
	}

}