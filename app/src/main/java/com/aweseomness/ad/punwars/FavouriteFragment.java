package com.aweseomness.ad.punwars;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
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

//WORK IN PROGRESS

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link //OnFragmentInteractionListener}
 * interface.
 */
public class FavouriteFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "section_number";

    // TODO: Rename and change types of parameters
    private int position;

    private TextView empty;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private View bloop;

    private OnFavChangedListener mCallback;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;
    private ProgressBar pb;

    static private ArrayList<String> favObjIds;

    // TODO: Rename and change types of parameters
    public static FavouriteFragment newInstance(int position) {
        FavouriteFragment fragment = new FavouriteFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavouriteFragment() {
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
        View view = inflater.inflate(R.layout.fragment_fav, container, false);
        pb = (ProgressBar)view.findViewById(R.id.progress_spinner);
        bloop = view;
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        empty = (TextView)view.findViewById(R.id.empty);


        refresh(false);
        return view;
    }

    public void refresh(final Boolean isFromElseWhere){
        if(pb==null){
            pb = (ProgressBar)bloop.findViewById(R.id.progress_spinner);
        }
        pb.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Friendships");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(parseObjects.size()==1) {
                    List<String> list1 = parseObjects.get(0).getList("favouriteArray");
                    final ArrayList<String> list;
                    if (list1!=null) list = new ArrayList<>(list1);
                    else list = new ArrayList<>();

                    if(list.size()>0 && ((isFromElseWhere &&!list.equals(favObjIds)) || (!isFromElseWhere))) {
                        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
                        for (String s : list) {
                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Attacks");
                            query.whereEqualTo("objectId", s);
                            queries.add(query);
                        }
                        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
                        mainQuery.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> parseObjects, ParseException e) {
                                if (parseObjects.size() > 0 && e == null) {
                                    empty.setText("");
                                    ArrayList<ParseObject> favourites = new ArrayList<>(parseObjects);
                                    mAdapter = new FavAdapter(getActivity(),
                                            R.layout.favourite_list_item, favourites);
                                    //TODO: Check if this works
                                    // Set the adapter
                                    mListView.setAdapter(mAdapter);
                                    favObjIds = new ArrayList<String>(list);
                                    pb.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                    else{
                        pb.setVisibility(View.GONE);
                        if(list.size()==0) empty.setText("You don't have any favourite puns ):\nClick the star at the attack and defence tabs,\nor the messages in your war log,\nto add to favourites!");
                    }
                }
            }
        });
    }

    /*public interface OnFavSelectedListener{
        public void refreshFav();
    }*/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnFavChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFriendSelectedListener");
        }
    }

    public interface OnFavChangedListener{
        public void refreshFav();
        public void changeTab(int tab);
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

    public class FavAdapter extends ArrayAdapter<ParseObject> {
        private int mResource;
        private ArrayList<ParseObject> mFavs;

        public FavAdapter(Context context, int resource, ArrayList<ParseObject> favs){
            super(context, resource, favs);
            this.mResource = resource;
            this.mFavs = favs;
        }
        @Override
        public View getView(int position, View row, ViewGroup parent){
            if (row==null){
                row = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
            }

            final ParseObject fav = mFavs.get(position);

            TextView punTv = (TextView)row.findViewById(R.id.punTv);
            TextView usernameTv = (TextView)row.findViewById(R.id.usernameTv);
            ImageView sword = (ImageView)row.findViewById(R.id.sword);
            ImageView shield = (ImageView)row.findViewById(R.id.shield);
            ImageButton unfavButt = (ImageButton)row.findViewById(R.id.minusFav);

            punTv.setText(fav.getString("pun"));

            if(fav.getString("fromUser").equals(ParseUser.getCurrentUser().getUsername())){
                usernameTv.setText(fav.getString("toUser"));
                sword.setVisibility(View.VISIBLE);
                shield.setVisibility(View.GONE);
            }
            else{
                usernameTv.setText(fav.getString("fromUser"));
                sword.setVisibility(View.GONE);
                shield.setVisibility(View.VISIBLE);
            }
            unfavButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
                    builder.setMessage("Are you sure you want to un-favourite this pun?")
                            .setTitle("Oh Dear")
                            .setPositiveButton("YUP", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pb.setVisibility(View.VISIBLE);
                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Friendships");
                                    query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                                    query.findInBackground(new FindCallback<ParseObject>() {
                                        @Override
                                        public void done(List<ParseObject> parseObjects, ParseException e) {
                                            if (parseObjects.size() == 1) {
                                                ParseObject friendship = parseObjects.get(0);
                                                friendship.removeAll("favouriteArray", Arrays.asList(fav.getObjectId()));
                                                friendship.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        refresh(false);
                                                        pb.setVisibility(View.GONE);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("nah", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
            return row;
        }
    }

}
