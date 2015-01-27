package com.example.xmpp;

import java.io.File;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends FragmentActivity {

	private static final String TAG = "XMPP";

	private static final String HOST = "talk.google.com";
	private static final int PORT = 5222;
	private static final String SERVICE = "gmail.com";

	private static final String USERNAME = "supervisor@iuyet.com.mx";
	private static final String PASSWORD = "*****";

	private static final String TO = "iuyet-csgit-cao@appspot.com";

	private ConnectionConfiguration connConfig;
	private XMPPConnection connection;

	private Button btn_sendMesaage;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btn_sendMesaage = (Button) findViewById(R.id.sendMessage);
		btn_sendMesaage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Message msg = new Message(TO, Message.Type.chat);
				msg.setBody("Hola Yoban");
				if(connection != null){
					connection.sendPacket(msg);
					Log.i(TAG, "Mensage enviado");
				}
			}
		});

		ConectarXMPP connect = new ConectarXMPP();
		connect.execute();

	}

	private class ConectarXMPP extends AsyncTask<Void, Void, Void>{

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog =  new ProgressDialog(MainActivity.this);
			dialog.setTitle(TAG);
			dialog.setMessage("Conectado con el Servidor...");
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			connConfig = new ConnectionConfiguration(HOST, PORT, SERVICE);
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
				connConfig.setTruststoreType("AndroidCAStore");
				connConfig.setTruststorePassword(null);
				connConfig.setTruststorePath(null);
			}else{
				connConfig.setTruststoreType("BKS");
				String path = System.getProperty("javax.net.ssl.trustStore");
				if(path == null){
					path = System.getProperty("java.home") + File.separator + "etc" + 
							File.separator + "security" + File.separator
							+ "cacerts.bks";
				}
				connConfig.setTruststorePath(path);
			}
			connection = new XMPPConnection(connConfig);

			try {
				connection.connect();
				Log.i(TAG, "Connected to: " + connection.getHost());
				connection.login(USERNAME, PASSWORD);
				Log.i(TAG, "Logged in as: " + connection.getUser());

			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getMessage());
				connection = null;
			}


			// Enviar mensage de presencia
			Presence presence = new Presence(Presence.Type.available);
			presence.setStatus("Disponible");
			connection.sendPacket(presence);
			Log.i(TAG, "Presence: " + presence.getStatus());

			setConnection();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(dialog.isShowing())
				dialog.dismiss();
		}
	}

	private void setConnection(){
		if(connection != null){
			// Agregar el listener para los mensages
			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			connection.addPacketListener(new PacketListener() {

				@Override
				public void processPacket(Packet packet) {
					// TODO Auto-generated method stub
					Message message = (Message) packet;
					if(message.getBody() != null){
						String to = StringUtils.parseBareAddress(message.getTo());
						String fromName = StringUtils.parseBareAddress(message.getFrom());
						String msg = StringUtils.parseBareAddress(message.getBody());
						Log.i(TAG, "From : " + fromName + " To: " + to +" MSG: "+ msg);
					}
				}
			}, filter);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		connection.disconnect();
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
}
