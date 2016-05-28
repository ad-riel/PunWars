package com.aweseomness.ad.punwars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.app.TabActivity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener,
                                                                FriendsFragment.OnFriendSelectedListener,
                                                                OnlineFragment.OnUserSelectedListener,
                                                                FavouriteFragment.OnFavChangedListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    static ArrayList<ParseUser> mFriends;
    static ArrayList<ParseUser> mUsers;
    static ArrayList<ParseObject> mFavourites;

    AttackFragment attackFrag;
    DefenceFragment defenceFrag;
    FavouriteFragment favFrag;
    FriendsFragment friendsFrag;
    OnlineFragment onlineFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null && users.size()>0) {
                    // Yay users found.
                    mUsers = new ArrayList<>(users);
                    mUsers.remove(ParseUser.getCurrentUser());
                    mFriends = new ArrayList<>();

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Friendships");
                    query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null && objects.size()==1) {
                                ParseObject friendship = objects.get(0);
                                final List<String> friendsUsernames = friendship.getList("friendsArray");
                                if(friendsUsernames !=null){
                                    for (int i = 0; i < friendsUsernames.size(); i++) {
                                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                                        query.whereEqualTo("username", friendsUsernames.get(i));
                                        query.findInBackground(new FindCallback<ParseUser>() {
                                            public void done(List<ParseUser> friends, ParseException e) {
                                                if (e == null && friends.size() == 1) {
                                                    // Yay users found.
                                                    mFriends.add(friends.get(0));
                                                    if(mFriends.size() == friendsUsernames.size()){

                                                    }
                                                }
                                                else {
                                                    //something wrong
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                            else{
                                //something wrong
                            }
                        }
                    });
                }
                else{
                    //something wrong
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ParseUser.getCurrentUser()!=null){
            //yay someone's logged in
        }
        else{
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void timeToRefresh(){
        /*FriendsFragment friendsFrag = (FriendsFragment)
                getSupportFragmentManager().findFragmentById(R.id.friends_frag);
        OnlineFragment onlineFrag = (OnlineFragment)getSupportFragmentManager().findFragmentById(R.id.online_frag);*/
        if(friendsFrag!=null) friendsFrag.refresh();
        if(onlineFrag!=null) onlineFrag.refresh();
    }

    public void refreshFav(){
        if(favFrag!=null) favFrag.refresh(true);
        if(attackFrag!=null) attackFrag.refresh();
        if(defenceFrag!=null) defenceFrag.refresh();
    }

    public void changeTab(int tab){
        mViewPager.setCurrentItem(tab);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_attack:
                /*Intent attackIntent = new Intent(MainActivity.this, WarLogActivity.class);
                attackIntent.putExtra("USERNAME", "poop");
                startActivity(attackIntent);*/
                mViewPager.setCurrentItem(3);
                return true;
                //TODO: new attack
            /*case R.id.action_settings:
                openSettings();
                return true;*/
            case R.id.action_logout:
                ParseUser.logOut();
                Intent logOutIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(logOutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return attackFrag = AttackFragment.newInstance(position + 1);
                case 1:
                    return defenceFrag = DefenceFragment.newInstance(position + 1);
                case 2:
                    return favFrag = FavouriteFragment.newInstance(position + 1);
                case 3:
                    return friendsFrag = FriendsFragment.newInstance(position + 1);
                case 4:
                    return onlineFrag = OnlineFragment.newInstance(position + 1);
            }
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "ATTACKS";
                case 1:
                    return "DEFENCE";
                case 2:
                    return "FAVOURITES";
                case 3:
                    return "RIVALS";
                case 4:
                    return "ALL PUNNERS";
            }
            return null;
        }
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteractionHome(Uri uri);
        public void openHome(View view);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
