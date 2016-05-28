package com.aweseomness.ad.punwars;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {//@link OnFragmentInteractionListener}
 * interface.
 */
public class FriendsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER="section_number";

    // TODO: Rename and change types of parameters
    private int position;
    private ProgressBar pb;
    private View bloop;


    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private OnFriendSelectedListener mCallback;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private FriendsAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static FriendsFragment newInstance(int position) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FriendsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            position = getArguments().getInt(ARG_SECTION_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_friends, container, false);
        pb = (ProgressBar)view.findViewById(R.id.progress_spinner);
        bloop = view;
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        refresh();
        //TODO: Check if this works

        return view;
    }

    public interface OnFriendSelectedListener {
        public void timeToRefresh();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnFriendSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFriendSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public void refresh(){
        if(pb==null) {
            pb = (ProgressBar)bloop.findViewById(R.id.progress_spinner);
        }
        TextView emptyTv = (TextView)bloop.findViewById(R.id.empty);
        pb.setVisibility(View.VISIBLE);
        if(MainActivity.mFriends.size()>0){
            emptyTv.setText("");
            mAdapter = new FriendsAdapter(getActivity().getApplicationContext(),
                    R.layout.friends_list_item, MainActivity.mFriends);
            mListView.setAdapter(mAdapter);
        }
        else {
            emptyTv.setText("You have no friends D:\n\nAdd new friends in the All Punners Tab! :D");
        }
        pb.setVisibility(View.GONE);
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */


    public class FriendsAdapter extends ArrayAdapter<ParseUser> {
        private int mResource;
        private ArrayList<ParseUser> mFriends;

        public FriendsAdapter(Context context, int resource, ArrayList<ParseUser> friends){
            super(context, resource, friends);
            this.mResource = resource;
            this.mFriends = friends;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent){
            if (row==null){
                row = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
            }

            final ParseUser user = mFriends.get(position);

            TextView userTv = (TextView)row.findViewById(R.id.userTv);
            ImageButton attackButt = (ImageButton)row.findViewById(R.id.attackButt);
            ImageButton remButt = (ImageButton)row.findViewById(R.id.remButt);

            userTv.setText(user.getUsername());
            attackButt.setVisibility(View.VISIBLE);
            attackButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent attackIntent = new Intent(getActivity(), WarLogActivity.class);
                    attackIntent.putExtra("USERNAME", user.getUsername());
                    startActivity(attackIntent);
                }
            });
            remButt.setVisibility(View.VISIBLE);
            remButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Friendships");
                    query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null && objects.size()==1) {
                                final ParseObject friendship = objects.get(0);
                                AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
                                builder.setMessage("Are you sure you want to unfriend "+user.getUsername()+" ?")
                                        .setTitle("Oh Dear")
                                        .setPositiveButton("YUP", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if(pb==null) pb = (ProgressBar)bloop.findViewById(R.id.progress_spinner);
                                                pb.setVisibility(View.VISIBLE);
                                                MainActivity.mFriends.remove(user);
                                                friendship.removeAll("friendsArray", Arrays.asList(user.getUsername()));
                                                friendship.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Friendships");
                                                        query1.whereEqualTo("username", user.getUsername());
                                                        query1.findInBackground(new FindCallback<ParseObject>() {
                                                            @Override
                                                            public void done(List<ParseObject> parseObjects, ParseException e) {
                                                                if (e == null && parseObjects.size() == 1) {
                                                                    final ParseObject friendship1 = parseObjects.get(0);
                                                                    friendship1.removeAll("friendsArray", Arrays.asList(ParseUser.getCurrentUser().getUsername()));
                                                                    friendship1.saveInBackground(new SaveCallback() {
                                                                        @Override
                                                                        public void done(ParseException e) {
                                                                            mCallback.timeToRefresh();
                                                                            Toast.makeText(getActivity(), user.getUsername() + " unfriended", Toast.LENGTH_SHORT).show();
                                                                            pb.setVisibility(View.GONE);
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        })
                                        .setNegativeButton("nah", null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                            else{
                                //something wrong
                            }
                        }
                    });
                }
            });

            return row;
        }
    }

}
