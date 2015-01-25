package chat.client.agent;

import android.content.ContentResolver;

/**
 * This interface implements the logic of the chat client running on the user
 * terminal.
 * 
 * @author Michele Izzo - Telecomitalia
 */

public interface ChatClientInterface {
	public void handleSpoken(String s);
	public String[] getParticipantNames(ContentResolver cr);
}