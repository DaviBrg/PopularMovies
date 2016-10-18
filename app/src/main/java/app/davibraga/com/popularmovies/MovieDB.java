package app.davibraga.com.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class MovieDB implements Parcelable{

    static private final String TITLE_FIELD = "title";
    static private final String OVERVIEW_FIELD = "overview";
    static private final String VOTE_AVERAGE_FIELD = "vote_average";
    static private final String POSTER_PATH_FIELD = "poster_path";
    static private final String RELEASE_DATE_FIELD = "release_date";
    static private final String POSTER_PATH_URL = "http://image.tmdb.org/t/p/w185";

    public static final Parcelable.Creator<MovieDB> CREATOR = new Parcelable.Creator<MovieDB>() {
        @Override
        public MovieDB createFromParcel(Parcel in) {
            return new MovieDB(in);
        }

        @Override
        public MovieDB[] newArray(int i) {
            return new MovieDB[i];
        }
    };

    private String title;
    private String overview;
    private String posterPath;
    private String releaseYear;
    private double voteAverage;



    public MovieDB(JSONObject movieJasonObject) throws JSONException {
        title = movieJasonObject.getString(TITLE_FIELD);
        overview = movieJasonObject.getString(OVERVIEW_FIELD);
        posterPath = POSTER_PATH_URL + movieJasonObject.getString(POSTER_PATH_FIELD);
        releaseYear = movieJasonObject.getString(RELEASE_DATE_FIELD).substring(0, 4);
        voteAverage = movieJasonObject.getDouble(VOTE_AVERAGE_FIELD);
    }

    private MovieDB(Parcel in) {
        title = in.readString();
        overview = in.readString();
        posterPath = in.readString();
        releaseYear = in.readString();
        voteAverage = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(title);
        out.writeString(overview);
        out.writeString(posterPath);
        out.writeString(releaseYear);
        out.writeDouble(voteAverage);
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
