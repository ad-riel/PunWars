package com.aweseomness.ad.punwars;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by adrieltan on 12/5/15.
 */
public class PunWars extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "rwzBei10GF6HruzEX43bkBeuVovFmNjDjdbvu4Pv", "bkSByDvLbGiqYj6dLttmzIbDzGJHCBgmg1P77Z1n");


        /*ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();*/
    }
}
