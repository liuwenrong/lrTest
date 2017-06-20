package com.coolyota.logreport.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;

/**
 * 图片解码器
 * Created by liuwenrong on 2017/6/2.
 */

public class ImageDecoder {

    public static Bitmap decode(File imageFile, int maxWidth, int maxHeight, ImageView.ScaleType scaleType) {
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap = null;
        if (maxWidth == 0 && maxHeight == 0) {
            decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), decodeOptions);
        } else {
            // If we have to resize this image, first get the natural bounds.
            decodeOptions.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;

            // Then compute the dimensions we would ideally like to decode to.
            int desiredWidth = getResizedDimension(maxWidth, maxHeight, actualWidth, actualHeight, scaleType);
            int desiredHeight = getResizedDimension(maxHeight, maxWidth, actualHeight, actualWidth, scaleType);

            // Decode to the nearest power of two scaling factor.
            decodeOptions.inJustDecodeBounds = false;
            // TODO(ficus): Do we need this or is it okay since API 8 doesn't support it?
            // decodeOptions.inPreferQualityOverSpeed = PREFER_QUALITY_OVER_SPEED;
            decodeOptions.inSampleSize = findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), decodeOptions);

            // If necessary, scale down to the maximal acceptable size.
            if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth || tempBitmap.getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }
        return bitmap;
    }

    private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary, ImageView.ScaleType scaleType) {
        // If no dominant value at all, just return the actual.
        if ((maxPrimary == 0) && (maxSecondary == 0)) {
            return actualPrimary;
        }

        // If ScaleType.FIT_XY fill the whole rectangle, ignore ratio.
        if (scaleType == ImageView.ScaleType.FIT_XY) {
            if (maxPrimary == 0) {
                return actualPrimary;
            }
            return maxPrimary;
        }

        // If primary is unspecified, scale primary to match secondary's scaling ratio.
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0) {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;

        // If ScaleType.CENTER_CROP fill the whole rectangle, preserve aspect ratio.
        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
            if ((resized * ratio) < maxSecondary) {
                resized = (int) (maxSecondary / ratio);
            }
            return resized;
        }

        if ((resized * ratio) > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }

    private static int findBestSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }
        return (int) n;
    }
}
