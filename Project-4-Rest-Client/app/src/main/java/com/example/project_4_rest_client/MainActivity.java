package com.example.project_4_rest_client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    JSONArray postJsonArray;
    JSONArray userJsonArray;
    ArrayList<String> postList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            this.loadPosts();
        }
    }

    //Loads the posts into the list view
    private void loadPosts() {
        ListView mainListView = (ListView)findViewById(R.id.mainListView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, postList);
        mainListView.setAdapter(arrayAdapter);

        getPosts();
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id){
                Intent intent = new Intent(MainActivity.this, PostActivity.class);

                try {
                    JSONObject postJson = postJsonArray.getJSONObject((int)id);
                    int userId = postJson.getInt("userId");
                    String username = "";

                    int userLength = userJsonArray.length();
                    for (int j = 0; j < userLength; j++) {
                        JSONObject userJson = userJsonArray.getJSONObject(j);
                        int curId = userJson.getInt("id");
                        if (userId == curId) {
                            username = userJson.getString("username");
                            break;
                        }
                    }

                    intent.putExtra("userId", userId);
                    intent.putExtra("username", username);
                    intent.putExtra("postId",  postJson.getInt("id"));
                    intent.putExtra("title", postJson.getString("title"));
                    intent.putExtra("body", postJson.getString("body"));

                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //Retrieves the posts from the server
    private void getPosts() {
        try {
            HttpClient httpclient = new DefaultHttpClient();

            HttpGet getPosts = new HttpGet("https://jsonplaceholder.typicode.com/posts");
            HttpResponse postResponse = httpclient.execute(getPosts);
            String postBodyString = EntityUtils.toString(postResponse.getEntity());

            HttpGet getUsers = new HttpGet("https://jsonplaceholder.typicode.com/users");
            HttpResponse userResponse = httpclient.execute(getUsers);
            String userBodyString = EntityUtils.toString(userResponse.getEntity());

            if(postResponse.getStatusLine().getStatusCode()==200 &&
                    userResponse.getStatusLine().getStatusCode()==200){

                postJsonArray = new JSONArray(postBodyString);
                userJsonArray = new JSONArray(userBodyString);

                int postLength = postJsonArray.length();
                int userLength = userJsonArray.length();
                Log.i("Server response", Integer.toString(postLength) );
                for (int i = 0; i < postLength; i++) {
                    JSONObject postJson = postJsonArray.getJSONObject(i);
                    int userId = postJson.getInt("userId");
                    String username = "";

                    for (int j = 0; j < userLength; j++) {
                        JSONObject userJson = userJsonArray.getJSONObject(j);
                        int id = userJson.getInt("id");
                        if (userId == id) {
                            username = userJson.getString("username");
                            break;
                        }
                    }

                    String listItemValue = "ID: " + postJson.getInt("id") + " Username: " +
                            username + "\n" + postJson.getString("title");
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

