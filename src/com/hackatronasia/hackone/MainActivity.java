package com.hackatronasia.hackone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalService;
import com.yotadevices.sdk.Drawer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "HackOne";
	private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
	private static final String CONFIG_CLIENT_ID = "AYshqRAmHBVt4wfL2qwdXZa8nqGnzMTB8j2q2ShMsCH0thXh_OlOub4Lku2q";
	private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
	private double Bal;

	private static PayPalConfiguration config = new PayPalConfiguration()
			.environment(CONFIG_ENVIRONMENT)
			.clientId(CONFIG_CLIENT_ID)
			// The following are only used in PayPalFuturePaymentActivity.
			.merchantName("Hipster Store")
			.merchantPrivacyPolicyUri(
					Uri.parse("https://www.example.com/privacy"))
			.merchantUserAgreementUri(
					Uri.parse("https://www.example.com/legal"));

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		Intent intent = new Intent(this, PayPalService.class);
		intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
		startService(intent);

	}

	public void onSendPay(View pressed) {
		Intent i = new Intent(MainActivity.this, ChooseActivity.class);
		startActivity(i);
		Log.i(TAG, "Send Monies Pressed");
	}

	public void onFuturePaymentPressed(View pressed) {
		Intent intent = new Intent(MainActivity.this,
				PayPalFuturePaymentActivity.class);
		Log.i(TAG, "Paypal Pressed");
		startActivityForResult(intent, REQUEST_CODE_FUTURE_PAYMENT);
	}

	public void onBalancesPressed(View pressed) {
		// Intent intent = new Intent(MainActivity.this, BalanceActivity.class);
		Log.e(TAG, "Balance Pressed");
		Drawer.Waveform wf = Drawer.Waveform.WAVEFORM_GC_PARTIAL;
		wf = Drawer.Waveform.WAVEFORM_A2;

		Intent i = new Intent(MainActivity.this, HelloWorldOnBackscreen.class);
		i.putExtra("waveform", wf.ordinal());
		
		i.putExtra("Balance",((MyApp) this.getApplication()).getBalances()); // get the balance from the server here.
		startService(i);
		// startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
			if (resultCode == Activity.RESULT_OK) {
				PayPalAuthorization auth = data
						.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
				if (auth != null) {
					try {
						Log.i("FuturePaymentExample", auth.toJSONObject()
								.toString(4));

						String authorization_code = auth.getAuthorizationCode();
						Log.i("FuturePaymentExample", authorization_code);
//						HttpAsyncTask httpAsyncTask = new HttpAsyncTask(
//								"http://kopispace.com:3000/user/auth/paypal",
//								authorization_code);
//						httpAsyncTask.execute();
						// sendAuthorizationToServer(auth);
						Toast.makeText(getApplicationContext(),
								"Future Payment code received from PayPal",
								Toast.LENGTH_LONG).show();

					} catch (JSONException e) {
						Log.e("FuturePaymentExample",
								"an extremely unlikely failure occurred: ", e);
					}
				}
			}
		}
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String url;
		private String authorization;

		public HttpAsyncTask(String url, String authorization) {
			this.url = url;
			this.authorization = authorization;
		}

		@Override
		protected String doInBackground(String... params) {
			Log.i(TAG, "Params[0]:" + this.url);
			// Log.i(TAG, "Params[0]:"+params[1]);
			return sendAuthorizationToServer(this.url, this.authorization);

		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG)
					.show();
		}
	}

	private static String sendAuthorizationToServer(String url,
			String authorization) {
		InputStream inputStream = null;
		String result = "";

		try {

			// 1. create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// 2. make POST request to the given URL
			HttpPost httpPost = new HttpPost(url);

			String json = "";

			// 3. build jsonObject
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("auth", authorization.toString());
			jsonObject.accumulate("user", "hardcodeduser");

			// 4. convert JSONObject to JSON to String
			json = jsonObject.toString();

			// 5. set json to StringEntity
			StringEntity se = new StringEntity(json);

			// 6. set httpPost Entity
			httpPost.setEntity(se);

			// 7. Set some headers to inform server about the type of the
			// content
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			// 8. Execute POST request to the given URL
			HttpResponse httpResponse = httpclient.execute(httpPost);

			// 9. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// 10. convert inputstream to string
			if (inputStream != null)
				result = convertInputStreamToString(inputStream);
			else
				result = "Did not work!";

		} catch (Exception e) {
			Log.d(TAG, e.getLocalizedMessage());
		}
		Log.d(TAG, result);
		return result;
	}

	private static String convertInputStreamToString(InputStream inputStream)
			throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;

	}
}