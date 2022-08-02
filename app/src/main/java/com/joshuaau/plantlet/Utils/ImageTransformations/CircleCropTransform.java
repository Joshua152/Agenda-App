/**
 * Picasso transform to circle crop an image
 */

package com.joshuaau.plantlet.Utils.ImageTransformations;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

public class CircleCropTransform implements Transformation {

    @Override
    public Bitmap transform(Bitmap source) {
        int xr = source.getWidth() / 2;
        int yr = source.getHeight() / 2;

        Bitmap mutableBitmap = Bitmap.createBitmap(xr * 2, yr * 2, source.getConfig());

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawCircle(xr, yr, Math.min(xr, yr), paint);

        source.recycle();

        return mutableBitmap;
    }

    @Override
    public String key() {
        return "Circle Transform";
    }
}
