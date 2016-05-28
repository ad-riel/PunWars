package com.aweseomness.ad.punwars;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.aweseomness.ad.punwars.dummy.DummyContent;
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
 * interface.
 */
public class OnlineFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "section_number";

    // TODO: Rename and change types of parameters
    private int position;

    private View bloop;

    private OnUserSelectedListener mCallback;
    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private UserAdapter mAdapter;

    private ProgressBar pb;

    // TODO: Rename and change types of parameters
    public static OnlineFragment newInstance(int sectionNumber) {
        OnlineFragment fragment = new OnlineFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OnlineFragment() {
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
        final View view = inflater.inflate(R.layout.fragment_online, container, false);
        pb = (ProgressBar)view.findViewById(R.id.progress_spinner);
        bloop = view;
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        refresh();
        return view;
    }

    public interface OnUserSelectedListener{
        public void timeToRefresh();
        public void changeTab(int tab);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnUserSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnUserSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void refresh(){
        if(pb==null){
            pb = (ProgressBar)bloop.findViewById(R.id.progress_spinner);
        }
        pb.setVisibility(View.VISIBLE);
        mAdapter = new UserAdapter(getActivity().getApplicationContext(),
                R.layout.user_list_item, MainActivity.mUsers, MainActivity.mFriends);
        //TODO: Check if this works
        // Set the adapter
        mListView.setAdapter(mAdapter);
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

    public class UserAdapter extends ArrayAdapter<ParseUser> {
        private int mResource;
        private ArrayList<ParseUser> mUsers;
        private ArrayList<ParseUser> mFriends;

        public UserAdapter(Context context, int resource, ArrayList<ParseUser> users, ArrayList<ParseUser> friendsOfCurUser){
            super(context, resource, users);
            this.mResource = resource;
            this.mUsers = users;
            this.mFriends = friendsOfCurUser;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent){
            if (row==null){
                row = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
            }

            final ParseUser user = mUsers.get(position);

            TextView userTv = (TextView)row.findViewById(R.id.userTv);
            ImageButton addButt = (ImageButton)row.findViewById(R.id.addButt);
            ImageButton attackButt = (ImageButton)row.findViewById(R.id.attackButt);

            userTv.setText(user.getUsername());

            if(mFriends!=null && mFriends.contains(user)){
                attackButt.setVisibility(View.VISIBLE);
                addButt.setVisibility(View.GONE);
                attackButt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent attackIntent = new Intent(getActivity(), WarLogActivity.class);
                        attackIntent.putExtra("USERNAME", user.getUsername());
                        startActivity(attackIntent);
                    }
                });
                return row;
            }
            else {
                addButt.setVisibility(View.VISIBLE);
                attackButt.setVisibility(View.GONE);
                addButt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pb.setVisibility(View.VISIBLE);
                        MainActivity.mFriends.add(user);

                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Friendships");
                        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                        query.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e == null && objects.size() == 1) {
                                    ParseObject friendship = objects.get(0);
                                    if (friendship.getList("friendsArray") == null)
                                        friendship.put("friendsArray", Arrays.asList(user.getUsername()));
                                    else
                                        friendship.addUnique("friendsArray", user.getUsername());
                                    friendship.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Friendships");
                                            query2.whereEqualTo("username", user.getUsername());
                                            query2.findInBackground(new FindCallback<ParseObject>() {
                                                public void done(List<ParseObject> objects, ParseException e) {
                                                    if (e == null && objects.size() == 1) {
                                                        ParseObject friendship = objects.get(0);
                                                        if (friendship.getList("friendsArray") == null) {
                                                            friendship.put("friendsArray", Arrays.asList(ParseUser.getCurrentUser().getUsername()));
                                                        } else {
                                                            friendship.addUnique("friendsArray", ParseUser.getCurrentUser().getUsername());
                                                        }
                                                        friendship.saveInBackground(new SaveCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if (e == null) {
                                                                    mCallback.timeToRefresh();
                                                                    mCallback.changeTab(3);
                                                                    Toast.makeText(getActivity(), user.getUsername()+" added as friend", Toast.LENGTH_SHORT).show();
                                                                    pb.setVisibility(View.GONE);
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        //something wrong
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
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

}
