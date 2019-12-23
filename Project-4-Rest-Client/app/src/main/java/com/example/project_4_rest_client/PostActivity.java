package com.example.project_4_rest_client;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    int userId;
    String username;
    int postId;
    String title;
    String body;
    ArrayList<String> commentList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Intent intent = getIntent();

        userId = intent.getIntExtra("userId", -1);
        username = intent.getStringExtra("username");
        postId = intent.getIntExtra("postId", -1);
        title = intent.getStringExtra("title");
        body = intent.getStringExtra("body");

        TextView usernameTextView = (TextView) findViewById(R.id.username);
        TextView titleTextView = (TextView) findViewById(R.id.title);
        TextView bodyTextView = (TextView) findViewById(R.id.body);

        usernameTextView.setText(username);
        titleTextView.setText(title);
        bodyTextView.setText(body);
        loadComments();
        setOnAddComment();
        setOnUsernameClick();
    }

    //Loads comments into list view
    private void loadComments() {
        ListView mainListView = (ListView)findViewById(R.id.comments);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, commentList);
        mainListView.setAdapter(arrayAdapter);

        getComments();
    }

    //Gets comments from the server
    private void getComments() {
        try {
            HttpClient httpclient = new DefaultHttpClient();

            HttpGet getComments = new HttpGet("https://jsonplaceholder.typicode.com/comments?postId=" + postId);
            HttpResponse commentResponse = httpclient.execute(getComments);
            String commentBodyString = EntityUtils.toString(commentResponse.getEntity());


            if(commentResponse.getStatusLine().getStatusCode()==200){

                JSONArray commentJsonArray = new JSONArray(commentBodyString);

                int commentLength = commentJsonArray.length();
                for (int i = 0; i < commentLength; i++) {
                    JSONObject commentJson = commentJsonArray.getJSONObject(i);
                    String listItemValue = commentJson.getString("email") + "\n" +
                            commentJson.getString("name") + "\n-----\n" + commentJson.getString("body");
                    commentList.add(listItemValue);
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

    //Creates the action for when the add comment button is pressed
    private void setOnAddComment() {
        Button addComment = findViewById(R.id.addComment);
        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                EditText emailInput = findViewById(R.id.emailInput);
                EditText nameInput = findViewById(R.id.nameInput);
                EditText bodyInput = findViewById(R.id.bodyInput);

                String email = emailInput.getText().toString();
                String name = nameInput.getText().toString();
                String body = bodyInput.getText().toString();

                if (email.length() > 0 && name.length() > 0 && body.length() > 0) {
                    emailInput.setText("");
                    nameInput.setText("");
                    bodyInput.setText("");

                    uploadComment(email, name, body);
                }
            }
        });
    }

    //Uploads a comment to the server then adds it to the list view
    private void uploadComment(String email, String name, String body) {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost postComment = new HttpPost("https://jsonplaceholder.typicode.com/comments");

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("postId", Integer.toString(postId)));
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("name", name));
            nameValuePairs.add(new BasicNameValuePair("body", body));
            postComment.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse commentResponse = httpclient.execute(postComment);

            if(commentResponse.getStatusLine().getStatusCode()==201){
                Toast.makeText(getApplicationContext(),"Upload Successful",Toast.LENGTH_SHORT).show();
                String listItemValue = email + "\n" + name + "\n-----\n" + body;
                commentList.add(listItemValue);
            } else {
                Log.i("Server response", "Failed to get server response" );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Loads the user activity on username click
    private void setOnUsernameClick() {
        TextView usernameTextView = findViewById(R.id.username);
        usernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(PostActivity.this, UserActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
    }
}
