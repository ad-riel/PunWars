package com.aweseomness.ad.punwars;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.List;


public class LoginActivity extends ActionBarActivity {

    private Button loginButt;
    private Button signupButt;
    private EditText usernameEt;
    private EditText passwordEt;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButt = (Button)findViewById(R.id.loginButt);
        signupButt = (Button)findViewById(R.id.signupButt);
        usernameEt = (EditText)findViewById(R.id.usernameEt);
        passwordEt = (EditText)findViewById(R.id.passwordEt);
        pb = (ProgressBar)findViewById(R.id.loading);

        pb.setVisibility(View.GONE);

        loginButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSupportProgressBarIndeterminateVisibility(true);
                ParseUser.logInInBackground(usernameEt.getText().toString(), passwordEt.getText().toString(), new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        setSupportProgressBarIndeterminateVisibility(false);
                        if (user != null) {
                            // Hooray! The user is logged in.
                            clearEt(true, true);
                            pb.setVisibility(View.VISIBLE);
                            Intent mainActIntent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(mainActIntent);
                        }
                        else {
                            // Signup failed. Look at the ParseException to see what happened.
                            ParseQuery<ParseUser> query = ParseUser.getQuery();
                            query.whereEqualTo("username", usernameEt.getText().toString());
                            query.findInBackground(new FindCallback<ParseUser>() {
                                public void done(List<ParseUser> objects, ParseException e) {
                                    if (objects.size()==0) {
                                        // Something went wrong.
                                        clearEt(true, true);
                                        AlertDialog.Builder builder =  new AlertDialog.Builder(LoginActivity.this);
                                        builder.setMessage("username not found! sign up to create a free account.")
                                                .setTitle("Oh Dear")
                                                .setPositiveButton("Sign Up", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        signup();
                                                    }
                                                })
                                                .setNegativeButton("nah", null);
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                    else{
                                        clearEt(false, true);
                                        showOhDearDialog("wrong password");
                                    }
                                }
                            });
                        }
                    }
                });

            }
        });
        signupButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    public void showOhDearDialog(String message){
        AlertDialog.Builder builder =  new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(message)
                .setTitle("Oh Dear")
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void signup(){
        Intent signupIntent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(signupIntent);
    }

    public void clearEt(boolean username, boolean pass) {
        if (pass) passwordEt.setText("");
        if(username) usernameEt.setText("");
    }

}
