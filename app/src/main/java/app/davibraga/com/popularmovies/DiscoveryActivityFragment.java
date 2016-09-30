package app.davibraga.com.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import org.json.JSONException;
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
        GridView rootView = (GridView) inflater.inflate(R.layout.fragment_discovery, container, false);


        movieDBAdapter = new MovieDBAdapter(getActivity(),new ArrayList<MovieDB>());

        rootView.setAdapter(movieDBAdapter);

        return rootView;
    }

    private void updateGrid() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defautOrderCriteria = getResources().getString(R.string.sort_by_popularity_value);
        String orderCriteria = sharedPref.getString(
                getResources().getString(R.string.order_by_list_key),
                defautOrderCriteria);
        new UpdateGridTask().execute(orderCriteria);
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

    class MovieDB {

        static private final String TITLE_FIELD = "title";
        static private final String OVERVIEW_FIELD = "overview";
        static private final String VOTE_AVERAGE_FIELD = "vote_average";
        static private final String POSTER_PATH_FIELD = "poster_path";
        static private final String RELEASE_DATE_FIELD = "release_date";
        static private final String POSTER_PATH_URL = "http://image.tmdb.org/t/p/w185";

        private String title;
        private String overview;
        private double voteAverage;
        private String posterPath;
        private String releaseYear;
        JSONObject jsonObject;

        public MovieDB(JSONObject movieJasonObject) throws JSONException {
        this.jsonObject = movieJasonObject;
            title = movieJasonObject.getString(TITLE_FIELD);
            overview = movieJasonObject.getString(OVERVIEW_FIELD);
            voteAverage = movieJasonObject.getDouble(VOTE_AVERAGE_FIELD);
            posterPath = POSTER_PATH_URL + movieJasonObject.getString(POSTER_PATH_FIELD);
            releaseYear = movieJasonObject.getString(RELEASE_DATE_FIELD).substring(0,4);
        }

        public String getTitle() {
            return title;
        }

        public String getOverview() {
            return overview;
        }

        public double getVoteAverage() {
            return voteAverage;
        }

        public String getPosterPath() {
            return posterPath;
        }

        public String getReleaseYear() {
            return releaseYear;
        }

    }

    URL getMoviesURL(String orderCriteria) {
        final String SCHEME = "http";
        final String MOVIE_AUTHORITY = "api.themoviedb.org";
        final String MOVIE_PATH = "/3/movie/";
        final String API_KEY_FIELD = "api_key";
        final String API_KEY_VALUE = "INSERT YOUR KEY HERE";
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

                    Resources res = getActivity().getResources();

                    intent.putExtra(res.getString(R.string.title_key),
                            movieDB.getTitle());

                    intent.putExtra(res.getString(R.string.overview_key),
                            movieDB.getOverview());

                    intent.putExtra(res.getString(R.string.vote_average_key),
                            Double.toString(movieDB.getVoteAverage()).substring(0,3));

                    intent.putExtra(res.getString(R.string.poster_path_key),
                            movieDB.getPosterPath());

                    intent.putExtra(getString(R.string.release_year_key),
                            movieDB.getReleaseYear());

                    startActivity(intent);
                }
            });

            return imageView;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_discovery_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id ==  R.id.action_update) {
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
