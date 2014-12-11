package com.hackatronasia.hackone;

import java.io.IOException;

import info.blockchain.api.APIException;
import info.blockchain.api.blockexplorer.*;
import com.yotadevices.sdk.Drawer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ChooseActivity extends Activity {
	private static final String TAG = "HackOne";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	private static final String[] DONATION_ADDRESSES_MAINNET = {
			"1L8WHukehukrRSTrFwX8jApwmWgfonM9Di",
			"1L8WHukehukrRSTrFwX8jApwmWgfonM9Di" };
	private static final String[] DONATION_ADDRESSES_TESTNET = {
			"n2LgzZ2nZuWmKnAtk845ttNMA6Wm7mXu8d",
			"n2LgzZ2nZuWmKnAtk845ttNMA6Wm7mXu8d" };

	private static final int REQUEST_CODE = 0;
	private NfcAdapter mNfcAdapter;
	private TextView donateMessage;
	private double Bal;
	private Boolean Toggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		performTAGOperations(getIntent());
		
		
		
		ToggleButton b = (ToggleButton) findViewById(R.id.toggleButton1);
		b.setChecked(((MyApp) ChooseActivity.this.getApplication()).getToggleButton());
		// attach an OnClickListener
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((MyApp) ChooseActivity.this.getApplication())
						.setToggleButton(!((MyApp) ChooseActivity.this
								.getApplication()).getToggleButton());
				// your click actions go here
			}
		});
		// when this activity is created first scan the NFC tag or start a
		// background process to continuously scan an NFC tag
		if (mNfcAdapter == null) {
			// Stop here, we definitely need NFC
			Toast.makeText(this, "This device doesn't support NFC.",
					Toast.LENGTH_LONG).show();
			finish();
			return;

		} else {
			// We can proceed as there is a NFC
			// Get the Toggle status if null or otherwise then proceed as normal
			// If set to true then call the Balance activity intent, deduct the
			// balance and set it as the new balance
			//set the address
			
			
		}

	}

	public void onSendPayPal(View pressed) {
		// Intent i = new Intent(MainActivity.this, ChooseActivity.class);
		// startActivity(i);
		// Call the endpoint with the amount that the NFC tag processed and the
		// email id within
		Log.i(TAG, "Send Via Paypal Pressed");

	}

	public void onSendBitcoin(View pressed) {
		// Intent i = new Intent(MainActivity.this, ChooseActivity.class);
		// startActivity(i);
		// Call the Bitcoin app here with the address scanned from the NFC.

		Log.i(TAG, "Send Via Bitcoin Pressed");
		handleDonate();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.choose, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Below methods copied from Donate Activity
	private String[] donationAddresses() {
		final boolean isMainnet = true;

		return isMainnet ? DONATION_ADDRESSES_MAINNET
				: DONATION_ADDRESSES_TESTNET;
	}

	public void handleDonate() {
		final String[] addresses = donationAddresses();

		BitcoinIntegration.requestForResult(ChooseActivity.this, REQUEST_CODE,
				addresses[0]);
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		try {
			if (requestCode == REQUEST_CODE) {
				if (resultCode == Activity.RESULT_OK) {
					final String txHash = BitcoinIntegration
							.transactionHashFromResult(data);
					if (txHash != null) {
						try{
							Log.d(TAG,"GOT BALANCE : "+txHash);
							((MyApp) this.getApplication()).setBalances(getBalanceFromTxHash(txHash.toString()));
							
						}
						catch(Exception E)
						{
							Log.e(TAG,"GETTING BALANCE ERROR:"+E);
						}
						final SpannableStringBuilder messageBuilder = new SpannableStringBuilder(
								"Transaction hash:\n");
						messageBuilder.append(txHash);
						messageBuilder.setSpan(new TypefaceSpan("monospace"),
								messageBuilder.length() - txHash.length(),
								messageBuilder.length(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

						if (BitcoinIntegration.paymentFromResult(data) != null)
							messageBuilder
									.append("\n(also a BIP70 payment message was received)");

						donateMessage.setText(messageBuilder);
						donateMessage.setVisibility(View.VISIBLE);
					}

					Toast.makeText(this, "Thank you!", Toast.LENGTH_LONG)
							.show();
				} else if (resultCode == Activity.RESULT_CANCELED) {
					Toast.makeText(this, "Cancelled.", Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(this, "Unknown result.", Toast.LENGTH_LONG)
							.show();
				}
			}
		} catch (Exception E) {
			Log.e(TAG, "Transaction Error:" + E.toString());
		}
	}

	//Blockchain Try
	public long getBalanceFromTxHash(String txHash) {
	    BlockExplorer blockExplorer = new BlockExplorer();
	    long finalBalance = 0;
	   Transaction tx;
	    try {
	        tx = blockExplorer.getTransaction("df67414652722d38b43dcbcac6927c97626a65bd4e76a2e2787e22948a7c5c47");
	        for (Input i : tx.getInputs()) {
	            //assume, if multiple inputs exist, all belongs to same accnt
	            Address address = blockExplorer.getAddress(i.getPreviousOutput().getAddress());
	            finalBalance += address.getFinalBalance();
	       }
	        System.out.println(finalBalance);
	        
	    } catch (APIException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	    
	    return finalBalance;
	}
	
	
	// The NFC intent calls begin here

	@Override
	protected void onResume() {
		super.onResume();

		/**
		 * It's important, that the activity is in the foreground (resumed).
		 * Otherwise an IllegalStateException is thrown.
		 */

		Log.e(TAG, "In Resume NFC");
		performTAGOperations(getIntent());
		setupForegroundDispatch(this, mNfcAdapter);
	}

	@Override
	protected void onPause() {
		/**
		 * Call this before onPause, otherwise an IllegalArgumentException is
		 * thrown as well.
		 */

		Log.e(TAG, "In Pause NFC");
		stopForegroundDispatch(this, mNfcAdapter);

		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		/**
		 * This method gets called, when a new Intent gets associated with the
		 * current activity instance. Instead of creating a new activity,
		 * onNewIntent will be called. For more information have a look at the
		 * documentation.
		 * 
		 * In our case this method gets called, when the user attaches a Tag to
		 * the device.
		 */
		Log.e(TAG, "Intent Found NFC");
		TextView BTCaddress = (TextView) findViewById(R.id.textView2);
		BTCaddress.setText(DONATION_ADDRESSES_MAINNET[0]+"/"+"  user1@paypal.com");
		TextView amount = (TextView) findViewById(R.id.textView4);
		amount.setText("Amount : 0.01 BTC/5 SGD");
		
		Log.e(TAG, "In Else");
		Toggle = ((MyApp) ChooseActivity.this.getApplication())
				.getToggleButton();
		Log.e(TAG, "In Else Toggle"+Toggle);
		if (Toggle) {

			Bal = ((MyApp) this.getApplication()).getBalances();
			// coffee at .01 BTC
			Log.e(TAG, "Got Balance NEW INTENT" + Bal);
			Bal = Bal - 0.01;
			((MyApp) this.getApplication()).setBalances(Bal);
			// Now call the Balance back screen intent
			Log.e(TAG, "Balance Automatic");
			Drawer.Waveform wf = Drawer.Waveform.WAVEFORM_GC_PARTIAL;
			wf = Drawer.Waveform.WAVEFORM_A2;

			Intent i = new Intent(ChooseActivity.this,
					HelloWorldOnBackscreen.class);
			i.putExtra("waveform", wf.ordinal());
			i.putExtra("Balance", Bal); // get the balance from the server
										// here.
			startService(i);
		}
		performTAGOperations(intent);
		Log.e(TAG, "In New NFC");
		handleIntent(intent);
	}
	
	private void performTAGOperations(Intent intent){
		Log.e(TAG, "In PERFORMTAG OPERATIONS");
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
		    String uri = intent.getDataString();
		    Log.d(TAG,"Inside Perform TAG Operations");
		    Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		    if (rawMsgs != null) {
		        NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
		        for (int i = 0; i < rawMsgs.length; i++) {
		            msgs[i] = (NdefMessage) rawMsgs[i];
		            Log.d(TAG,"MSGS:"+msgs[i]);
		        }
		        //get the Address and the Amoount and call The Balance intent from here, after checking if toggle is on. Else just update PAYTO
		    }
		    TextView BTCaddress = (TextView) findViewById(R.id.textView2);
			BTCaddress.setText(DONATION_ADDRESSES_MAINNET[0]+"/"+"  user1@paypal.com");
			TextView amount = (TextView) findViewById(R.id.textView4);
			amount.setText("Amount : 0.01 BTC/5 SGD");
			
			Log.e(TAG, "In Else");
			Toggle = ((MyApp) ChooseActivity.this.getApplication())
					.getToggleButton();
			Log.e(TAG, "In Else Toggle"+Toggle);
			if (Toggle) {

				Bal = ((MyApp) this.getApplication()).getBalances();
				// coffee at .01 BTC
				Log.e(TAG, "Got Balance" + Bal);
				Bal = Bal - 0.01;
				// Now call the Balance back screen intent
				Log.e(TAG, "Balance Automatic");
				Drawer.Waveform wf = Drawer.Waveform.WAVEFORM_GC_PARTIAL;
				wf = Drawer.Waveform.WAVEFORM_A2;

				Intent i = new Intent(ChooseActivity.this,
						HelloWorldOnBackscreen.class);
				i.putExtra("waveform", wf.ordinal());
				i.putExtra("Balance", Bal); // get the balance from the server
											// here.
				startService(i);
			}
		}
	}
	private void handleIntent(Intent intent) {
		// TODO: handle Intent
	}

	/**
	 * @param activity
	 *            The corresponding {@link Activity} requesting the foreground
	 *            dispatch.
	 * @param adapter
	 *            The {@link NfcAdapter} used for the foreground dispatch.
	 */
	public static void setupForegroundDispatch(final Activity activity,
			NfcAdapter adapter) {
		final Intent intent = new Intent(activity.getApplicationContext(),
				activity.getClass());
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		final PendingIntent pendingIntent = PendingIntent.getActivity(
				activity.getApplicationContext(), 0, intent, 0);

		IntentFilter[] filters = new IntentFilter[1];
		String[][] techList = new String[][] {};
		Log.e(TAG, "In Forward Dispatcher");
		
	
		
		
		// Notice that this is the same filter as in our manifest.
		filters[0] = new IntentFilter();
		filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
		filters[0].addCategory(Intent.CATEGORY_DEFAULT);
		try {
			filters[0].addDataType(MIME_TEXT_PLAIN);
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("Check your mime type.");
		}

		adapter.enableForegroundDispatch(activity, pendingIntent, filters,
				techList);
	}

	/**
	 * @param activity
	 *            The corresponding {@link BaseActivity} requesting to stop the
	 *            foreground dispatch.
	 * @param adapter
	 *            The {@link NfcAdapter} used for the foreground dispatch.
	 */
	public static void stopForegroundDispatch(final Activity activity,
			NfcAdapter adapter) {
		adapter.disableForegroundDispatch(activity);
	}

}
