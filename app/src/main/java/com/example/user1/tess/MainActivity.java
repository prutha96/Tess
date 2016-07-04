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
    static final int REQUEST_IMAGE_SELECT = 2;

    Button click;
    Button select;
    ImageView imageView;
    //TextView textView;
    String path;
    public static final String TAG = "PermissionTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        click = (Button) findViewById(R.id.click);
        select = (Button) findViewById(R.id.select);
        imageView = (ImageView) findViewById(R.id.imageView);
        //textView = (TextView) findViewById(R.id.textView);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void dispatchTakePictureIntent(View view1) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    protected void dispatchSelectPictureIntent(View view2) {
        boolean StoragePermissionGranted = isStoragePermissionGranted();

        if (StoragePermissionGranted) {
            Intent selectPictureIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(selectPictureIntent, REQUEST_IMAGE_SELECT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //imageView.setImageBitmap(imageBitmap);

            try {
                path = saveImageToInternalStorage(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //textView.setText(path);

            try {
                // imageBitmap = fixImage(imageBitmap, path);
            } catch (Exception e) {
                //textView.setText("IOException");
                e.printStackTrace();
            }

            loadImageFromStorage(path);

            performOCR(imageBitmap);
        }else{
           try {
               Uri uri = data.getData();
               System.out.println("uri:::" + uri.getPath());
               Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
               System.out.println("bitmap:::" + imageBitmap);
               if (null != uri) {
                   imageView.setImageURI(uri);
               }
               performOCR(imageBitmap);
           }catch (Exception ex){
               ex.printStackTrace();
           }
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

    /*
    Bitmap fixImage(Bitmap bitmap, String path) throws IOException {
        ExifInterface exif = new ExifInterface(path);

        int exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
        );
        int rotate = 0;

        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
        }

        if (rotate != 0) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            //Setting pre rotate
            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);

            //Rotating bitmap and converting to ARGB_8888
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        }

        //bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        return bitmap;
    }
    */

    void performOCR(Bitmap bitmap) {
        try {
            boolean StoragePermissionGranted = isStoragePermissionGranted();

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

            //textView.setText(recognisedText);
            Intent text = new Intent(this, DisplayText.class);
            text.putExtra(EXTRA_MESSAGE, recognisedText);
            startActivity(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}