package com.antonio.sunshine;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.antonio.sunshine.data.SunshinePreferences;
import com.antonio.sunshine.utilities.NetworkUtils;
import com.antonio.sunshine.utilities.OpenWeatherJsonUtils;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView mWeatherTextView;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);


        mWeatherTextView = findViewById(R.id.tv_weather_data);
        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        //Call loadWeatherData to perform the network request to get the weather
        /* Once all of our views are setup, we can load the weather data. */
        loadWeatherData();
    }


    //Create a method that will get the user's preferred location and execute your new AsyncTask and call it loadWeatherData
    /**
     * This method will get the user's preferred location for weather, and then tell some
     * background method to get the weather data in the background.
     */
    private void loadWeatherData(){
        //Call showWeatherDataView before executing the AsyncTask
        showWeatherDataView();

        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
    }

    private void showWeatherDataView(){
        /* First, make sure error is invisible*/
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mWeatherTextView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(){
        /* First, hide the current visible data*/
        mWeatherTextView.setVisibility(View.INVISIBLE);

        /* Then, show the error*/
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


     /*Class that extends AsyncTask to perform network requests*/
    public class FetchWeatherTask extends AsyncTask<String, Void, String []>{

         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             mLoadingIndicator.setVisibility(View.VISIBLE);
         }


         @Override
         protected String[] doInBackground(String... params) {

             /* if there's no zip code, there's nothing to look up. */
             if (params.length == 0){
                 return null;
             }

             String location = params[0];
             URL weatherRequestUrl = NetworkUtils.buildUrl(location);

             try {
                 String jsonWeatherResponse = NetworkUtils
                         .getResponseFromHttpUrl(weatherRequestUrl);
                 String [] simpleJsonWeatherData = OpenWeatherJsonUtils
                         .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                 return simpleJsonWeatherData;
             } catch (Exception e) {
                 e.printStackTrace();
                 return null;
             }
         }

         /*Override the onPostExecute method to display the results of the network request*/
         @Override
        protected void onPostExecute(String [] weatherData){

             /*Hide loading indicator as soon as data is finished loading*/
             mLoadingIndicator.setVisibility(View.INVISIBLE);

             if (weatherData != null){

                 //If the weather data was not null, make sure the data view is visible

                 showWeatherDataView();
                  /*
+                 * Iterate through the array and append the Strings to the TextView. The reason why we add
+                 * the "\n\n\n" after the String is to give visual separation between each String in the
+                 * TextView. Later, we'll learn about a better way to display lists of data.
+                 */
                  for (String weatherString: weatherData){
                      mWeatherTextView.append((weatherString) + "\n\n\n");
                  }
             }else {
                 //if data was null show error message
                 showErrorMessage();
             }
         }
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();

        /* Use the inflator's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);

        /* Return true so that the menu is displayed in the Toolbar */

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_refresh){
            mWeatherTextView.setText("");
            loadWeatherData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
