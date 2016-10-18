package app.davibraga.com.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment {

    private static final int TITLE_BIG_SIZE = 35;
    private static final float SMALLER_TEXT_SIZE = 25;

    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final String maxGrade = "/10";
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        Resources res = activity.getResources();

        MovieDB movieDB =  intent.getParcelableExtra(res.getString(R.string.movie_data_key));

        TextView titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
        String title = movieDB.getTitle();
        if (title.length() > TITLE_BIG_SIZE) {
            titleTextView.setTextSize(SMALLER_TEXT_SIZE);
        }
        titleTextView.setText(title);

        TextView yearTextView = (TextView) rootView.findViewById(R.id.yearTextView);
        yearTextView.setText(movieDB.getReleaseYear());

        TextView voteTextView = (TextView) rootView.findViewById(R.id.voteTextView);
        String grade = Double.toString(movieDB.getVoteAverage()).substring(0,3) + maxGrade;
        voteTextView.setText(grade);

        TextView overviewTextView = (TextView) rootView.findViewById(R.id.overviewTextView);
        overviewTextView.setText(movieDB.getOverview());

        ImageView posterImageView = (ImageView) rootView.findViewById(R.id.posterImageView);
        Picasso.with(getContext())
                .load(movieDB.getPosterPath())
                .into(posterImageView);

        return rootView;
    }
}
