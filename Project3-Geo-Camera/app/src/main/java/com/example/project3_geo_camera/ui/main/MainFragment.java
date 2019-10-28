package com.example.project3_geo_camera.ui.main;

import android.content.Intent;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.project3_geo_camera.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class MainFragment extends Fragment {

    public static MainFragment newInstance() {
        return new MainFragment();
    }
    private FusedLocationProviderClient fusedLocationClient;
    private static SimpleDateFormat dateTimeFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.main_fragment, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        dateTimeFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

        //Handles take photo button on click
        Button takePhotoButton = rootView.findViewById(R.id.takePhotoButton);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("myDebug", "onClick: ");
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                dispatchTakePictureIntent();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    //Handles coming back from camera intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
        }
    }

    String mCurrentPhotoPath;

    //Creates the file for the image to go into
    private File createImageFile() throws IOException {
        File storageDir = Environment.getExternalStorageDirectory();
        File image = File.createTempFile("geo-camera", ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //Creates and starts the take picture activity with implicit intent
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    //Add the taken image to the main gallery on the phone and adds exif data
    private void galleryAddPic() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            File f = new File(mCurrentPhotoPath);
                            Uri contentUri = Uri.fromFile(f);

                            ExifInterface exif = null;
                            try {
                                exif = new ExifInterface(f.getAbsolutePath());
                                exif.setAttribute(ExifInterface.TAG_DATETIME,
                                        dateTimeFormat.format(new Date(System.currentTimeMillis())) );
                                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
                                        LocationToExif.convert(location.getLatitude()));
                                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,
                                        LocationToExif.latitudeRef(location.getLatitude()));
                                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
                                        LocationToExif.convert(location.getLongitude()));
                                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,
                                        LocationToExif.longitudeRef(location.getLongitude()));
                                exif.saveAttributes();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            mediaScanIntent.setData(contentUri);
                            getActivity().sendBroadcast(mediaScanIntent);
                            getActivity().recreate();
                        }
                    }
                });
    }
}
