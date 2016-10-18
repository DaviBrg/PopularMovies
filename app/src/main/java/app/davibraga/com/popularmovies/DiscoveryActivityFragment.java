package app.davibraga.com.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DiscoveryActivityFragment extends Fragment {

    private static final String DISCOVERY_ACTIVITY_ERROR_TAG = "DiscoveryActivityError";
    private MovieDBAdapter movieDBAdapter;

    public DiscoveryActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        GridView rootView = (GridView) inflater.inflate(R.layout.fragment_discovery,
                container,
                false);


        movieDBAdapter = new MovieDBAdapter(getActivity(),new ArrayList<MovieDB>());

        rootView.setAdapter(movieDBAdapter);

        return rootView;
    }

    String getCurrentOrderCriteria() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defautOrderCriteria = getResources().getString(R.string.sort_by_popularity_value);
        return  sharedPref.getString(
                getResources().getString(R.string.order_by_list_key),
                defautOrderCriteria);
    }

    private void updateGrid() {
        new UpdateGridTask().execute(getCurrentOrderCriteria());
    }

    @Override
    public void onStart() {
        super.onStart();
        updateGrid();
    }

    private static String httpGET(URL url) {
        HttpURLConnection httpConnection = null;
        StringBuilder builder = new StringBuilder();
        String result;

        try {
            httpConnection = (HttpURLConnection) url.openConnection();
            BufferedInputStream stream = new BufferedInputStream(httpConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            result = builder.toString();
        }
        catch (IOException ex) {
            result = "";
        }
        finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return result;
    }



    class UpdateGridTask extends AsyncTask<String ,Void, ArrayList<MovieDB>>  {


        @Override
        protected ArrayList<MovieDB> doInBackground(String... strings) {
            URL url = getMoviesURL(strings[0]);
            String json = httpGET(url);
            if (json.equals("")) {
                return null;
            }
            return getMoviesArray(json);
        }


        @Override
        protected void onPostExecute(ArrayList<MovieDB> movieDBs) {
            if (movieDBs != null) {
                movieDBAdapter.clear();
                movieDBAdapter.addAll(movieDBs);
            }
            else {
                connectionErrorToast();
            }
        }
    }

    URL getMoviesURL(String orderCriteria) {
        final String SCHEME = "http";
        final String MOVIE_AUTHORITY = "api.themoviedb.org";
        final String MOVIE_PATH = "/3/movie/";
        final String API_KEY_FIELD = "api_key";
        final String API_KEY_VALUE = "645d19544dbaf839eb48f98e561347bc";
        final String LANGUAGE_FIELD = "language";

        URL url = null;
        try {

            String moviesURLString = new Uri.Builder().scheme(SCHEME)
                    .authority(MOVIE_AUTHORITY)
                    .path(MOVIE_PATH + orderCriteria)
                    .appendQueryParameter(API_KEY_FIELD, API_KEY_VALUE)
                    .appendQueryParameter(LANGUAGE_FIELD, getResources()
                            .getString(R.string.language_code))
                    .build()
                    .toString();

            url = new URL(moviesURLString);
        } catch (Exception ex) {
            unexpectedErrorToast();
        }
        return url;
    }

    ArrayList<MovieDB> getMoviesArray(String jsonString) {

        final String MOVIE_ARRAY_FIELD = "results";
        ArrayList<MovieDB> moviesArray = new ArrayList<>();

        try {
            JSONObject object = new JSONObject(jsonString);
            JSONArray jsonArray = object.getJSONArray(MOVIE_ARRAY_FIELD);
            for (int i=0; i<jsonArray.length(); i++){
                moviesArray.add(new MovieDB(jsonArray.getJSONObject(i)));
            }
        }
        catch (Exception ex) {
            unexpectedErrorToast();
        }
        return  moviesArray;
    }


    private class MovieDBAdapter extends ArrayAdapter<MovieDB> {

        public MovieDBAdapter(Activity context, List<MovieDB> movieDBs) {
            super(context, 0, movieDBs);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final MovieDB movieDB = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater
                        .from(getContext())
                        .inflate(R.layout.movie_image_view, parent, false);
            }

            ImageView imageView = (ImageView) convertView;

            Picasso.with(getContext()).load(movieDB.getPosterPath()).into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(),DetailsActivity.class);
                    intent.putExtra(getString(R.string.movie_data_key), movieDB);
                    startActivity(intent);
                }
            });

            return imageView;
        }
    }

    boolean isOrderPopularity() {
        String popularityOrder = getResources().getString(R.string.sort_by_popularity_value);
        return getCurrentOrderCriteria().equals(popularityOrder);
    }

    boolean isOrderHighestRated() {
        String highestRatedOrder = getResources().getString(R.string.sort_by_highest_rated_value);
        return getCurrentOrderCriteria().equals(highestRatedOrder);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_discovery_fragment, menu);

        if (isOrderPopularity()) {
            String highestRatedTitle = getResources().getString(R.string.toggle_highest_rated);
            menu.findItem(R.id.action_toggle).setTitle(highestRatedTitle);

        }
        else if(isOrderHighestRated()) {
            String popularityTitle = getResources().getString(R.string.toggle_popularity);
            menu.findItem(R.id.action_toggle).setTitle(popularityTitle);
        }

    }

    private void setOrderBy(String order) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPref.edit()
                .putString(getResources().getString(R.string.order_by_list_key),order)
                .apply();
    }

    private void setOrderByPopularity(){
        String order = getResources().getString(R.string.sort_by_popularity_value);
        setOrderBy(order);
    }

    private void setOrderByHighestRated() {
        String order = getResources().getString(R.string.sort_by_highest_rated_value);
        setOrderBy(order);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_toggle  && isOrderHighestRated()) {
            setOrderByPopularity();
            String newTitle = getResources().getString(R.string.toggle_highest_rated);
            item.setTitle(newTitle);
            updateGrid();
            return true;
        }
        if (id == R.id.action_toggle  && isOrderPopularity()) {
            setOrderByHighestRated();
            String newTitle = getResources().getString(R.string.toggle_popularity);
            item.setTitle(newTitle);
            updateGrid();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void unexpectedErrorToast() {
        Toast.makeText(getActivity().getApplicationContext(),
                getResources().getString(R.string.unexpected_error),
                Toast.LENGTH_SHORT).show();
    }

    void connectionErrorToast() {
        Toast.makeText(getActivity().getApplicationContext(),
                getResources().getString(R.string.connection_error),
                Toast.LENGTH_SHORT).show();
    }

}
