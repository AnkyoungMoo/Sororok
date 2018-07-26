package com.example.km.sororok.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.Toast;

import com.example.km.sororok.R;
import com.example.km.sororok.adapter.GalleryAdapter;

import java.util.ArrayList;

public class UserGalleryActivity extends AppCompatActivity {

    private static GridView gridGallery;
    private static GalleryAdapter galleryAdapter;
    private static ArrayList<String> galleryImageUrls;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acrivity_user_gallery);

        Intent intent = getIntent();

        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
            initComponent();
            fetchGalleryImages();
            setUpGridView();
        }


    }

    private void fetchGalleryImages() {

        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;//order data by date
        Cursor imgCursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy + " DESC");

        galleryImageUrls = new ArrayList<>();//Init array

        for (int i = 0; i < imgCursor.getCount(); i++) {
            imgCursor.moveToPosition(i);
            int dataColumnIndex = imgCursor.getColumnIndex(MediaStore.Images.Media.DATA);//get column index
             galleryImageUrls.add(imgCursor.getString(dataColumnIndex));//get Image from column index
             System.out.println("Array path : " + galleryImageUrls.get(i));
        }
    }

    private void setUpGridView() {
        galleryAdapter = new GalleryAdapter(UserGalleryActivity.this, galleryImageUrls);
        gridGallery.setAdapter(galleryAdapter);
    }

    public void initComponent(){
        gridGallery = (GridView)findViewById(R.id.grid_user_gallery);
    }


    //일정 수준의 버전 확인
    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    100);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                100);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}
