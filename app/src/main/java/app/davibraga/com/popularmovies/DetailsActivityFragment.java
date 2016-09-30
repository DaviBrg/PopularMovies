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


        TextView titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
        titleTextView.setText(intent.getStringExtra(res.getString(R.string.title_key)));


        TextView yearTextView = (TextView) rootView.findViewById(R.id.yearTextView);
        yearTextView.setText(intent.getStringExtra(res.getString(R.string.release_year_key)));

        TextView voteTextView = (TextView) rootView.findViewById(R.id.voteTextView);
        String grade = intent.getStringExtra(res.getString(R.string.vote_average_key)) + maxGrade;
        voteTextView.setText(grade);

        TextView overviewTextView = (TextView) rootView.findViewById(R.id.overviewTextView);
        overviewTextView.setText(intent.getStringExtra(res.getString(R.string.overview_key)));

        ImageView posterImageView = (ImageView) rootView.findViewById(R.id.posterImageView);
        Picasso.with(getContext())
                .load(intent.getStringExtra(res.getString(R.string.poster_path_key)))
                .into(posterImageView);

        return rootView;
    }
}
