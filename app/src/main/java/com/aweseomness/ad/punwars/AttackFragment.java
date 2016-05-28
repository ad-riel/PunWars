package com.aweseomness.ad.punwars;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.Space;
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
import java.util.Calendar;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link //OnFragmentInteractionListener}
 * interface.
 */
public class AttackFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "section_number";
   // private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int position;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    private TextView empty;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;
    private ProgressBar pb;
    private FavouriteFragment.OnFavChangedListener mCallback;

    // TODO: Rename and change types of parameters
    public static AttackFragment newInstance(int sectionNumber) {
        AttackFragment fragment = new AttackFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AttackFragment() {
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
        final View view = inflater.inflate(R.layout.fragment_attack, container, false);

        pb = (ProgressBar)view.findViewById(R.id.progress_spinner);
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        empty = (TextView)view.findViewById(R.id.empty);

        refresh();
        return view;
    }

    public void refresh(){
        pb.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Attacks");
        query.whereEqualTo("fromUser", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if(objects.size()>0) {
                        empty.setText("");
                        // Yay attacks found.
                        final ArrayList<ParseObject> attacks = new ArrayList<>(objects);

                        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Friendships");
                        query1.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                        query1.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> person, ParseException e) {
                                if(person.size()==1){
                                    List<String> leList = person.get(0).getList("favouriteArray");
                                    ArrayList<String> favs;
                                    if(leList!=null) favs = new ArrayList<>(leList);
                                    else {
                                        favs = null;
                                    }
                                    mAdapter = new AttackAdapter(getActivity().getApplicationContext(),
                                            R.layout.attack_list_item, attacks, favs);
                                    //TODO: Check if this works
                                    // Set the adapter
                                    mListView.setAdapter(mAdapter);
                                    // Set OnItemClickListener so we can be notified on item clicks
                                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            Intent warLogIntent = new Intent(getActivity(), WarLogActivity.class);
                                            warLogIntent.putExtra("USERNAME", attacks.get(position).getString("toUser"));
                                            startActivity(warLogIntent);
                                        }
                                    });
                                    pb.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                    else {
                        pb.setVisibility(View.GONE);
                        empty.setText("Looks like you haven't attacked anyone. Yet.\nClick the rivals tab to start wrecking havoc! ;)");
                    }
                }
                else{
                    //something wrong
                }
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (FavouriteFragment.OnFavChangedListener) activity;
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //TODO: stuff
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
*/

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

    public class AttackAdapter extends ArrayAdapter<ParseObject> {
        private int mResource;
        private ArrayList<ParseObject> mAttacks;
        private ArrayList<String> mFavs;

        public AttackAdapter(Context context, int resource, ArrayList<ParseObject> attacks, ArrayList<String> favs){
            super(context, resource, attacks);
            this.mResource = resource;
            this.mAttacks = attacks;
            this.mFavs = favs;
        }
        @Override
        public View getView(int position, View row, ViewGroup parent){
            if (row==null){
                row = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
            }

            final ParseObject attack = mAttacks.get(position);

            TextView userTv = (TextView)row.findViewById(R.id.usernameTv);
            TextView punTv = (TextView)row.findViewById(R.id.punTv);
            ImageButton swordButt = (ImageButton)row.findViewById(R.id.swordButt);
            ImageButton shieldButt = (ImageButton)row.findViewById(R.id.shieldButt);
            ImageButton favButt = (ImageButton)row.findViewById(R.id.favButt);
            ImageView favImg = (ImageView)row.findViewById(R.id.favImg);

            punTv.setText(attack.getString("pun"));
            userTv.setText(attack.getString("toUser"));
            shieldButt.setVisibility(View.GONE);
            swordButt.setVisibility(View.VISIBLE);
            swordButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent attackIntent = new Intent(getActivity(), WarLogActivity.class);
                    attackIntent.putExtra("USERNAME", attack.getString("toUser"));
                    startActivity(attackIntent);
                }
            });
            if(mFavs!=null && mFavs.contains(attack.getObjectId())){
                favButt.setVisibility(View.GONE);
                favImg.setVisibility(View.VISIBLE);
            }
            else {
                favButt.setVisibility(View.VISIBLE);
                favImg.setVisibility(View.GONE);
                favButt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pb.setVisibility(View.VISIBLE);

                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Friendships");
                        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                        query.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e == null && objects.size() == 1) {
                                    ParseObject friendship = objects.get(0);
                                    if (friendship.getList("favouriteArray") == null) {
                                        friendship.put("favouriteArray", Arrays.asList(attack.getObjectId()));
                                    } else
                                        friendship.addUnique("favouriteArray", attack.getObjectId());
                                    friendship.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            Toast.makeText(getActivity(), "Pun added to favourites!", Toast.LENGTH_SHORT).show();
                                            mCallback.refreshFav();
                                            mCallback.changeTab(2);
                                            pb.setVisibility(View.GONE);

                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }

            return row;
        }
    }
}
