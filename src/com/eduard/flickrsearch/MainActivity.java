package com.eduard.flickrsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

  ProgressDialog progressDialog;
  BackgroundThread backgroundThread;



  FlickrImage[] myFlickrImage;

  int pageNr = 1;


  public int increasePageNr(int nr) {
    pageNr = pageNr + nr;
    return pageNr;
  }

  public int decreasePageNr(int nr) {
    pageNr = pageNr - nr;
    return pageNr;
  }

  String FlickrQuery_url = "https://api.flickr.com/services/rest/?method=flickr.photos.search";
  String FlickrQuery_per_page = "&per_page=10";
  String FlickrQuery_page = "&page=" + Integer.toString(pageNr);
  String FlickrQuery_nojsoncallback = "&nojsoncallback=1";
  String FlickrQuery_format = "&format=json";
  String FlickrQuery_tag = "&tags=";
  String FlickrQuery_key = "&api_key=";

  String FlickrApiKey = "bd29e274e9626faabb31ab494ba6c84b";

  final String DEFAULT_SEARCH = "betsafe";

  EditText searchText;

  Button searchButton;
  Button nextPageButton;
  Button prevPageButton;
  Button searchAgainButton;

  ListView photoBar;
  Bitmap bmFlickr;

  @SuppressWarnings("deprecation")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    searchText = (EditText) findViewById(R.id.searchtext);
    searchText.setText(DEFAULT_SEARCH);

    searchButton = (Button) findViewById(R.id.searchbutton);

    nextPageButton = (Button) findViewById(R.id.next);
    nextPageButton.setVisibility(View.INVISIBLE);

    prevPageButton = (Button) findViewById(R.id.prev);
    prevPageButton.setVisibility(View.INVISIBLE);

    searchAgainButton = (Button) findViewById(R.id.searchAgain);
    searchAgainButton.setVisibility(View.INVISIBLE);

    photoBar = (ListView) findViewById(R.id.photobar);

    searchButton.setOnClickListener(searchButtonOnClickListener);
    nextPageButton.setOnClickListener(nextPageButtonOnClickListener);
    prevPageButton.setOnClickListener(prevPageButtonOnClickListener);
    searchAgainButton.setOnClickListener(searchAgainButtonOnClickListener);

  }


  private Button.OnClickListener searchButtonOnClickListener = new Button.OnClickListener() {
    public void onClick(View view) {
      InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
      FlickrQuery_page = "&page=1"; // Set it to page 1;
      progressDialog = ProgressDialog.show(MainActivity.this, "ProgressDialog", "Loading!");
      backgroundThread = new BackgroundThread();
      backgroundThread.setRunning(true);
      backgroundThread.start();
    }

  };

  private Button.OnClickListener nextPageButtonOnClickListener = new Button.OnClickListener() {
    public void onClick(View view) {
      try {
        prevPageButton.setVisibility(View.VISIBLE);
        FlickrQuery_page = "&page=" + Integer.toString(increasePageNr(1)); // Change page, increase
        progressDialog = ProgressDialog.show(MainActivity.this, "ProgressDialog", "Loading page " + pageNr);
        backgroundThread = new BackgroundThread();
        backgroundThread.setRunning(true);
        backgroundThread.start();
      } catch (Exception e) {
        System.out.println(e);
      }
    }
  };

  private Button.OnClickListener prevPageButtonOnClickListener = new Button.OnClickListener() {
    public void onClick(View arg0) {
      try {
        if (pageNr != 1) {
          FlickrQuery_page = "&page=" + Integer.toString(decreasePageNr(1)); // Change page,
                                                                             // decrease pageNr with
                                                                             // 1
          progressDialog = ProgressDialog.show(MainActivity.this, "ProgressDialog", "Loading page " + pageNr);
          backgroundThread = new BackgroundThread();
          backgroundThread.setRunning(true);
          backgroundThread.start();
        }
      } catch (Exception e) {
        System.out.println(e);
      }
    }
  };

  // Resets view
  private Button.OnClickListener searchAgainButtonOnClickListener = new Button.OnClickListener() {
    public void onClick(View arg0) {
      try {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
      } catch (Exception e) {
        System.out.println(e);
      }
    }
  };

  private String QueryFlickr(String q) {
    String qResult = null;
    System.out.println(FlickrQuery_page);
    String qString =
        FlickrQuery_url + FlickrQuery_per_page + FlickrQuery_page + FlickrQuery_nojsoncallback + FlickrQuery_format + FlickrQuery_tag + q + FlickrQuery_key
            + FlickrApiKey;
    System.out.println(qString);
    HttpClient httpClient = new DefaultHttpClient();
    HttpGet httpGet = new HttpGet(qString);

    try {
      HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();

      if (httpEntity != null) {
        InputStream inputStream = httpEntity.getContent();
        Reader in = new InputStreamReader(inputStream);
        BufferedReader bufferedreader = new BufferedReader(in);
        StringBuilder stringBuilder = new StringBuilder();

        String stringReadLine = null;

        while ((stringReadLine = bufferedreader.readLine()) != null) {
          stringBuilder.append(stringReadLine + "\n");
        }

        qResult = stringBuilder.toString();
        inputStream.close();
      }

    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println(qResult);

    return qResult;
  }

  private FlickrImage[] ParseJSON(String json) {

    FlickrImage[] flickrImage = null;

    bmFlickr = null;
    String flickrId;
    String flickrOwner;
    String flickrSecret;
    String flickrServer;
    String flickrFarm;
    String flickrTitle;

    try {
      JSONObject JsonObject = new JSONObject(json);
      JSONObject Json_photos = JsonObject.getJSONObject("photos");
      JSONArray JsonArray_photo = Json_photos.getJSONArray("photo");

      flickrImage = new FlickrImage[JsonArray_photo.length()];
      for (int i = 0; i < JsonArray_photo.length(); i++) {
        JSONObject FlickrPhoto = JsonArray_photo.getJSONObject(i);
        flickrId = FlickrPhoto.getString("id");
        flickrOwner = FlickrPhoto.getString("owner");
        flickrSecret = FlickrPhoto.getString("secret");
        flickrServer = FlickrPhoto.getString("server");
        flickrFarm = FlickrPhoto.getString("farm");
        flickrTitle = FlickrPhoto.getString("title");
        flickrImage[i] = new FlickrImage(flickrId, flickrOwner, flickrSecret, flickrServer, flickrFarm, flickrTitle);
      }

    } catch (JSONException e) {
      e.printStackTrace();
    }

    return flickrImage;
  }

  public class BackgroundThread extends Thread {
    volatile boolean running = false;
    int cnt;

    void setRunning(boolean b) {
      running = b;
      cnt = 10;
    }

    @Override
    public void run() {
      String searchQ = searchText.getText().toString().replaceAll("\\s", "_");  // Replace searchtext whitespace with "_"
      String searchResult = QueryFlickr(searchQ);

      myFlickrImage = ParseJSON(searchResult);
      
      handler.sendMessage(handler.obtainMessage());
    }
  }

  Handler handler = new Handler() {

    @Override
    public void handleMessage(Message msg) {

      progressDialog.dismiss();
      photoBar.setAdapter(new FlickrAdapter(MainActivity.this, myFlickrImage));
      if (myFlickrImage.length != 0) {
        if (pageNr == 1) {
          prevPageButton.setVisibility(View.INVISIBLE);  // Turn invisible when pageNr is 1
        }
        nextPageButton.setVisibility(View.VISIBLE);
        searchAgainButton.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);
        searchText.setVisibility(View.GONE);
        Toast.makeText(MainActivity.this, "Flickr images loaded", Toast.LENGTH_LONG).show();
      } else {
        nextPageButton.setVisibility(View.INVISIBLE);
        Toast.makeText(MainActivity.this, "No images found", Toast.LENGTH_LONG).show();
      }
    }
  };

}
