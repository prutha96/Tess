package com.example.user1.tess;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by user1 on 7/5/2016.
 */
public class Processing {

    Bitmap convertToARGB8888(Bitmap bitmap) {

        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        return bitmap;
    }

    Bitmap thresholding(Bitmap bitmap) {

        int pixel, A, R, G, B, grey, threshold = 128;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                pixel = bitmap.getPixel(i, j);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                grey = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);

                if (grey > threshold)
                    grey = 255;
                else
                    grey = 0;

                bitmap.setPixel(i, j, Color.argb(A, grey, grey, grey));
            }
        }

        return bitmap;
    }

    Bitmap sharpen(Bitmap bitmap) {
        if (bitmap != null) {

            Convolution convolution = new Convolution();

            long startTime = System.currentTimeMillis();
            bitmap = convolution.convBitmap(bitmap);
            long duration = System.currentTimeMillis() - startTime;
        }
        return bitmap;
    }
}
