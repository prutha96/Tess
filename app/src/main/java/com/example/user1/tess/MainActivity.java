package com.example.user1.tess;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.example.user1.tess.MESSAGE";

    static final int REQUEST_IMAGE_CAPTURE = 1;

    Button click;
    ImageView imageView;
    String path;
    public static final String TAG = "PermissionTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        click = (Button) findViewById(R.id.click);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    protected void dispatchTakePictureIntent(View view1) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            try {
                path = saveImageToInternalStorage(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            loadImageFromStorage(path);

            performOCR(imageBitmap);
        }

    }

    public String saveImageToInternalStorage(Bitmap image) throws IOException {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "image.png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        fos.close();
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path) {

        try {
            File f = new File(path, "image.png");
            Bitmap imageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
            imageView.setImageBitmap(imageBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void performOCR(Bitmap bitmap) {
        try {

            TessBaseAPI baseAPI = new TessBaseAPI();

            String datapath = Environment.getExternalStorageDirectory() + "/tesseract/";
            String language = "eng";
            File dir = new File(datapath + "tessdata/");
            if (!dir.exists())
                dir.mkdirs();
            baseAPI.init(datapath, language);

            baseAPI.setImage(bitmap);
            String recognisedText = baseAPI.getUTF8Text();
            baseAPI.end();

            Intent text = new Intent(this, DisplayText.class);
            text.putExtra(EXTRA_MESSAGE, recognisedText);
            startActivity(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}