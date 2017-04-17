package com.smartstar.christopherjohnson.tamagatchi;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by christopher.johnson on 4/12/17.
 *
 * Updates the Tamagatchi's hunger and health levels in the background
 */

public class BackgroundIntent extends IntentService {

    public BackgroundIntent() {
        super("BackgroundIntent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
