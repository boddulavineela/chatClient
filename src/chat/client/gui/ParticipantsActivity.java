/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package chat.client.gui;

import java.util.ArrayList;
import java.util.logging.Level;

import chat.client.agent.ChatClientInterface;
import jade.core.MicroRuntime;
import jade.util.Logger;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.provider.ContactsContract;
import android.widget.Toast;

/**
 * This activity implement the participants interface.
 * 
 * @author Michele Izzo - Telecomitalia
 */

public class ParticipantsActivity extends ListActivity {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	private MyReceiver myReceiver;

	private String nickname;
	private ChatClientInterface chatClientInterface;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			nickname = extras.getString("nickname");
		}

		try {
			chatClientInterface = MicroRuntime.getAgent(nickname)
					.getO2AInterface(ChatClientInterface.class);
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ControllerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		myReceiver = new MyReceiver();

		IntentFilter refreshParticipantsFilter = new IntentFilter();
		refreshParticipantsFilter
				.addAction("jade.demo.chat.REFRESH_PARTICIPANTS");
		registerReceiver(myReceiver, refreshParticipantsFilter);

		setContentView(R.layout.participants);

		setListAdapter(new ArrayAdapter<String>(this, R.layout.participant,
				chatClientInterface.getParticipantNames(getContentResolver())));

		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		listView.setOnItemClickListener(listViewtListener);
	}

    private OnItemClickListener listViewtListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO: A partecipant was picked. Send a private message.

            final String participant = chatClientInterface.getParticipantNames(getContentResolver())[position];

            if (participant.charAt(participant.length()-2) == 'N') {
                new AlertDialog.Builder(ParticipantsActivity.this)
                        .setTitle("Create Contact")
                        .setMessage("Create contact for " + participant.substring(0, participant.length() - 4) + " ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue
                                addContact(participant.substring(0, participant.length() - 4));

                                Context context = getApplicationContext();
                                CharSequence text = "Contact created!";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();

                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();


            }
            else {
                finish();
            }
            // TODO for CSC750:
            // 1. getParticipantnames
            // 2. Check if in contacts [N]
            // 3. A popup to confirm add to contacts
            // 4. add to contacts!
		}
	};

    private void addContact(String participant) {
        String DisplayName = participant;

        ArrayList <ContentProviderOperation> ops = new ArrayList < ContentProviderOperation > ();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
        if (DisplayName != null) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            DisplayName).build());
        }

        // Asking the Contact provider to create a new contact
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            // Toast.makeText(myContext, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(myReceiver);

		logger.log(Level.INFO, "Destroy activity!");
	}

	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logger.log(Level.INFO, "Received intent " + action);
			if (action.equalsIgnoreCase("jade.demo.chat.REFRESH_PARTICIPANTS")) {
				setListAdapter(new ArrayAdapter<String>(
						ParticipantsActivity.this, R.layout.participant,
						chatClientInterface.getParticipantNames(getContentResolver())));
			}
		}
	}

}
