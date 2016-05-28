package com.aweseomness.ad.punwars;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;


public class SignupActivity extends ActionBarActivity {
    private Button signupButt;
    private Button cancelButt;

    private EditText usernameEt;
    private EditText passwordEt;
    private EditText cfmpassEt;
    private EditText emailEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupButt = (Button)findViewById(R.id.signupButt);
        usernameEt = (EditText)findViewById(R.id.usernameEt);
        passwordEt = (EditText)findViewById(R.id.passwordEt);
        cfmpassEt = (EditText)findViewById(R.id.password2Et);
        emailEt = (EditText)findViewById(R.id.emailEt);
        cancelButt = (Button)findViewById(R.id.cancelButt);

        cancelButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(backIntent);
            }
        });

        signupButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSupportProgressBarIndeterminateVisibility(true);
                if(passwordEt.getText().toString().equals(cfmpassEt.getText().toString())) {
                    // SIGNING UP
                    final ParseUser user = new ParseUser();
                    user.setUsername(usernameEt.getText().toString());
                    user.setPassword(passwordEt.getText().toString());
                    if(!emailEt.getText().toString().equals("")) user.setEmail(emailEt.getText().toString());

                    // other fields can be set just like with ParseObject
                    //user.put("phone", "650-253-0000");

                    user.signUpInBackground(new SignUpCallback() {
                        public void done(ParseException e) {
                            setSupportProgressBarIndeterminateVisibility(false);
                            if (e == null) {
                                // Hooray! Let them use the app now.
                                ParseObject futureFriends = new ParseObject("Friendships");
                                futureFriends.put("username", user.getUsername());
                                futureFriends.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        ParseUser.logInInBackground(user.getUsername(), passwordEt.getText().toString(), new LogInCallback() {
                                            public void done(ParseUser user, ParseException e) {
                                                if (user != null) {
                                                    // Hooray! The user is logged in.
                                                    clearEt(true, true, true);
                                                    Intent mainActIntent = new Intent(SignupActivity.this, MainActivity.class);
                                                    startActivity(mainActIntent);
                                                } else {
                                                    // Signup failed. Look at the ParseException to see what happened.
                                                    showOhDearDialog(e.getMessage());
                                                }
                                            }
                                        });
                                    }
                                });
                            } else {
                                // Sign up didn't succeed. Look at the ParseException
                                // to figure out what went wrong
                                showOhDearDialog(e.getMessage());
                                clearEt(false, false, true);
                            }
                        }
                    });
                }
                else {
                    showOhDearDialog("Your passwords don't match");
                    clearEt(false, false, true);
                }

                //
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signup, menu);
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
        AlertDialog.Builder builder =  new AlertDialog.Builder(SignupActivity.this);
        builder.setMessage(message)
                .setTitle("Oh Dear")
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void clearEt(boolean username, boolean email, boolean pass) {
        if (pass) {
            passwordEt.setText("");
            cfmpassEt.setText("");
        }
        if(email) emailEt.setText("");
        if(username) usernameEt.setText("");
    }
}