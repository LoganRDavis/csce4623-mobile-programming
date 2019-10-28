package com.example.project3_geo_camera.ui.main;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.example.project3_geo_camera.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Date;

public class MapHandlerFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener {

    MapView mMapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;


    public static MapHandlerFragment newInstance() {
        return new MapHandlerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_fragment, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.setMyLocationEnabled(true);
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                googleMap.setOnInfoWindowClickListener(MapHandlerFragment.this);
                loadImageMap();
            }
        });

        return rootView;
    }

    public void loadImageMap(){
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        LatLng currentLocation = new LatLng(location.getLatitude(),
                                location.getLongitude());
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(currentLocation).zoom(12).build();
                        googleMap.animateCamera(CameraUpdateFactory
                                .newCameraPosition(cameraPosition));
                    }
                });
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.LATITUDE,
                MediaStore.Images.ImageColumns.LONGITUDE
        };
        final Cursor cursor = getContext().getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        Log.d("myDebug", "onMapReady: HERE");
        try {
            while (cursor.moveToNext()) {
                String imagePath = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                Long imageDateMs = cursor.getLong(
                        cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
                Date imageDate = new Date(imageDateMs);
                String imageLatitude = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE));
                String imageLongitude = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE));

                Log.d("myDebug", "IMAGE");
                if (imageDate != null && imageLatitude != null && imageLongitude != null){
                    Log.d("myDebug", "onMapReady: Latitude=" + imageLatitude + " Longitude=" + imageLongitude);
                    LatLng imgLocation = new LatLng(Double.valueOf(imageLatitude), Double.valueOf(imageLongitude));

                    Marker mMarker = googleMap.addMarker(new MarkerOptions()
                            .position(imgLocation)
                            .title(String.valueOf(imageDate))
                            .snippet("Click here to view.")
                    );
                    mMarker.setTag(imagePath);
                }
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (googleMap != null) {
            googleMap.clear();
            loadImageMap();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        String imagePath = (String) marker.getTag();
        Log.d("myDebug", "onMarkerClick: " + imagePath);
        Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);

        View dialogView = getLayoutInflater().inflate(R.layout.image_layout, null);

        Dialog settingsDialog = new Dialog(getActivity());
        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        settingsDialog.setContentView(dialogView);
        ImageView img= (ImageView) settingsDialog.findViewById(R.id.selectedImage);
        img.setImageBitmap(myBitmap);
        settingsDialog.show();
    }
}
