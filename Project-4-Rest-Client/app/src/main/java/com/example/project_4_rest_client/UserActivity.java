package com.example.project_4_rest_client;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class UserActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap gmap;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    int userId;
    double longitude;
    double latitude;
    ArrayList<String> postList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        Intent intent = getIntent();

        userId = intent.getIntExtra("userId", -1);
        getUser();
        loadPosts();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        Log.d("my", Double.toString(latitude));
        LatLng location = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(location)
                .title("Location"));
        gmap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

    //Retrieves the user information
    private void getUser() {
        try {
            HttpClient httpclient = new DefaultHttpClient();

            HttpGet getUser = new HttpGet("https://jsonplaceholder.typicode.com/users/" + userId);
            HttpResponse userResponse = httpclient.execute(getUser);
            String userBodyString = EntityUtils.toString(userResponse.getEntity());
            JSONObject userJson = new JSONObject(userBodyString);
            longitude = Double.parseDouble(userJson.getJSONObject("address").getJSONObject("geo").getString("lng"));
            latitude = Double.parseDouble(userJson.getJSONObject("address").getJSONObject("geo").getString("lat"));

            TextView userInfoView = (TextView) findViewById(R.id.userInfo);

            String userInfo = userJson.getString("name") + "\n" + userJson.getString("username") +
                    "\n" + userJson.getString("email") + "\n" + userJson.getString("phone") + "\n" +
                    userJson.getString("website");

            userInfoView.setText(userInfo);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Loads the posts made by the user into the list view
    private void loadPosts() {
        ListView mainListView = (ListView)findViewById(R.id.posts);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, postList);
        mainListView.setAdapter(arrayAdapter);

        getPosts();
    }

    //Retrieves the posts from the server
    private void getPosts() {
        try {
            HttpClient httpclient = new DefaultHttpClient();

            HttpGet getPosts = new HttpGet("https://jsonplaceholder.typicode.com/posts?userId=" + userId);
            HttpResponse postResponse = httpclient.execute(getPosts);
            String postBodyString = EntityUtils.toString(postResponse.getEntity());


            if(postResponse.getStatusLine().getStatusCode()==200){

                JSONArray postJsonArray = new JSONArray(postBodyString);

                int postLength = postJsonArray.length();
                for (int i = 0; i < postLength; i++) {
                    JSONObject postJson = postJsonArray.getJSONObject(i);
                    String listItemValue = postJson.getString("title");
                    postList.add(listItemValue);
                }
            } else {
                Log.i("Server response", "Failed to get server response" );
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
