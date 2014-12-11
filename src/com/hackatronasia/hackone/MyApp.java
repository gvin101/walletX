package com.hackatronasia.hackone;

import android.app.Application;
import android.util.Log;

public class MyApp extends Application {

	private static Boolean ToggleButton = false;
	private double Balances = 3;


	public Boolean getToggleButton() {
		return ToggleButton;
	}

	public void setToggleButton(Boolean toggleButton) {
		Log.d("HackOne","Setting Boolean:"+toggleButton);
		ToggleButton = toggleButton;
	}

	public double getBalances() {
		return Balances;
	}

	public void setBalances(double balances) {
		Balances = balances;
	}
}
