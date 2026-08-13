package com.amikom.bumdesma.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

public class ImageUtils {

    /** Crop bitmap jadi bulat sempurna, dipakai avatar dashboard & profil */
    public static Bitmap buatBitmapBulat(Bitmap source, int diameterPx) {
        int ukuranSisi = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - ukuranSisi) / 2;
        int y = (source.getHeight() - ukuranSisi) / 2;
        Bitmap persegi = Bitmap.createBitmap(source, x, y, ukuranSisi, ukuranSisi);
        Bitmap discale = Bitmap.createScaledBitmap(persegi, diameterPx, diameterPx, true);

        Bitmap output = Bitmap.createBitmap(diameterPx, diameterPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new BitmapShader(discale, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        float radius = diameterPx / 2f;
        canvas.drawCircle(radius, radius, radius, paint);
        return output;
    }
}