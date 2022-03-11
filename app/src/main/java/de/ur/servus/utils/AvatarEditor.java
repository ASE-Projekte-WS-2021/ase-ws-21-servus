package de.ur.servus.utils;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.ur.servus.R;

public class AvatarEditor {

    private final Activity activity;
    private final ContextWrapper cw;

    public AvatarEditor(Activity activity) {
        this.activity = activity;
        this.cw = new ContextWrapper(activity.getApplicationContext());
    }

    public Bitmap fetchImageFromStorage(Uri photoUri) {
        Bitmap image = null;
        try {
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(activity.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), photoUri);
            }

            image = cropBitmapToSquare(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public void saveProfilePicture(Bitmap image) {
        File directory = cw.getDir("images", MODE_PRIVATE);
        if(!directory.exists()) directory.mkdir();
        File file = new File(directory, "profile_picture.png");

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 80, fos);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap loadProfilePicture() {
        File directory = cw.getDir("images", MODE_PRIVATE);
        File file = new File(directory, "profile_picture.png");

        Bitmap profilePicture = BitmapFactory.decodeResource(activity.getResources(), R.drawable.img_placeholder_avatar);
        if (file.exists()) {
            profilePicture = BitmapFactory.decodeFile(file.toString());
        }
        return profilePicture;
    }

    private Bitmap cropBitmapToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap result;
        if (height > width) {
            result = Bitmap.createBitmap(bitmap, 0, height / 2 - width / 2, width, width); //Warning because of 'width' wording
        } else {
            result = Bitmap.createBitmap(bitmap, width / 2 - height / 2, 0, height, height); //Warning because of 'height' wording
        }
        result = resizeBitmap(result);

        bitmap.recycle();
        return result;
    }

    private Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) 256) / width;
        float scaleHeight = ((float) 256) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    }
}
