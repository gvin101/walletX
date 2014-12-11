package com.hackatronasia.hackone;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.yotadevices.sdk.BSActivity;

public class HelloWorldOnBackscreen extends BSActivity {

	private double bal = 0;
	private static final String TAG = "HackOne";

	@Override
	protected void onBSCreate() {
		super.onBSCreate();
		Log.e(TAG, "Back Screen Called");

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			bal = extras.getDouble("Balance");

		}

		setBSContentView(R.layout.activity_balance);
		Log.e(TAG, "Setting Balance" + bal);
		TextView btcView = (TextView) findViewById(R.id.TextViewBTC);
		try {
			Log.e(TAG, "Found The view");
			btcView.setText(Double.toString(bal));
		} catch (Exception E) {
			Log.e(TAG, "Error in setting Text:" + E.toString());
		}
	}

	// The NFC activity will call this directly

	// Create another actvity for the NFC

}
