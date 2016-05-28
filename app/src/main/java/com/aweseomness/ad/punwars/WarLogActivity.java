package com.aweseomness.ad.punwars;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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


public class WarLogActivity extends ActionBarActivity {
    private String username;
    private ParseUser user;
    private ArrayList<ParseObject> attacks;
    private Button sendButt;
    private EditText punEt;
    private Handler handler;

    private ProgressBar pb;
    private ProgressBar spinner;

    private PunAdapter mAdapter;
    private ListView mLv;

    private static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_war_log);
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        sendButt = (Button)findViewById(R.id.send_button);
        punEt = (EditText)findViewById(R.id.punEt);
        pb = (ProgressBar)findViewById(R.id.loading_spinner);
        spinner = (ProgressBar)findViewById(R.id.spinner);

        attacks = new ArrayList<>();
        handler = new Handler();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", username);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null && (users.size()==1)){
                    //check if this is a unique username to see if it's valid
                    user = users.get(0);

                    getSupportActionBar().setTitle(username);
                    mLv = (ListView) findViewById(R.id.list);
                    mAdapter = new PunAdapter(WarLogActivity.this, R.layout.warlog_list_item, attacks);
                    mLv.setAdapter(mAdapter);
                    refresh();
                }
                else{
                    //something wrong
                }
            }
        });

        sendButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendButt.setVisibility(View.GONE);
                spinner.setVisibility(View.VISIBLE);
                String pun = punEt.getText().toString();
                ParseObject newAttack = new ParseObject("Attacks");
                newAttack.put("fromUser", ParseUser.getCurrentUser().getUsername());
                newAttack.put("toUser", username);
                newAttack.put("pun", pun);
                newAttack.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        punEt.setText("");
                        sendButt.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);
                    }
                });
            }
        });

        handler.postDelayed(runnable, 100);
    }

    public void setFavDialog(ParseObject pun){
        final ParseObject attack = pun;
        AlertDialog.Builder builder =  new AlertDialog.Builder(WarLogActivity.this);
        builder.setMessage("Favourite this pun?")
                .setTitle("Yay")
                .setPositiveButton("YUP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                                            Toast.makeText(WarLogActivity.this, "Pun added to favourites!", Toast.LENGTH_SHORT).show();
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

    private Runnable runnable = new Runnable(){
        @Override
                public void run() {
            refresh();
            handler.postDelayed(this, 100);
        }
    };

    public void refresh(){
        if(ParseUser.getCurrentUser()!=null) {
            List<ParseQuery<ParseObject>> leList = new ArrayList<ParseQuery<ParseObject>>();
            ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Attacks");
            query1.whereEqualTo("fromUser", ParseUser.getCurrentUser().getUsername());
            query1.whereEqualTo("toUser", username);
            leList.add(query1);

            ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Attacks");
            query2.whereEqualTo("fromUser", username);
            query2.whereEqualTo("toUser", ParseUser.getCurrentUser().getUsername());
            leList.add(query2);

            ParseQuery<ParseObject> queries = ParseQuery.or(leList);
            queries.orderByAscending("createdAt");
            queries.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);
            queries.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        ArrayList<ParseObject> newAttacks = new ArrayList<>(parseObjects);
                        if (!newAttacks.equals(attacks)) {
                            attacks.clear();
                            attacks.addAll(parseObjects);
                            mAdapter.notifyDataSetInvalidated();
                            mLv.invalidate();

                            mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    setFavDialog(attacks.get(position));
                                }
                            });
                        }
                    }
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_war_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class PunAdapter extends ArrayAdapter<ParseObject> {
        private int mResource;
        private ArrayList<ParseObject> mAttacks;

        public PunAdapter(Context context, int resource, ArrayList<ParseObject> attacks){
            super(context, resource, attacks);
            this.mResource = resource;
            this.mAttacks = attacks;
        }
        @Override
        public View getView(int position, View row, ViewGroup parent){
            if (row==null){
                row = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
            }

            ParseObject attack = mAttacks.get(position);

            TextView punTv = (TextView)row.findViewById(R.id.punTv);
            ImageView shieldImg = (ImageView)row.findViewById(R.id.shieldimg);
            ImageView swordImg = (ImageView)row.findViewById(R.id.swordimg);

            punTv.setText(attack.getString("pun"));

            if(attack.getString("fromUser").equals(ParseUser.getCurrentUser().getUsername())){
                shieldImg.setVisibility(View.INVISIBLE);
                punTv.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            }
            else if(attack.getString("toUser").equals(ParseUser.getCurrentUser().getUsername())){
                swordImg.setVisibility(View.INVISIBLE);
                punTv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            }

            return row;
        }
    }
}
