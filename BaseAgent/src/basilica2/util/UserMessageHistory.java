package basilica2.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import basilica2.agents.events.MessageEvent;
import edu.cmu.cs.lti.basilica2.core.Event;
import edu.cmu.cs.lti.project911.utils.log.Logger;

public class UserMessageHistory {

	private final String USER_POKE_PROPERTIES_FILE = "UserPoke.properties";
	private ArrayList<MessageEvent> messages = new ArrayList<MessageEvent>();
	private ArrayList<MessageEvent> multi_message_response_array = new ArrayList<MessageEvent>();
	private boolean userPoked = false;
	private Properties properties;
	
	private String user_poke_message="";
	private int short_message_threshold = 4; 
	private int messages_to_wait = 1;
	private String multi_line_message_indicator = "...";
	
	
	public UserMessageHistory() {
		loadPropertieFile();
		user_poke_message = properties.getProperty("user_poke_message", "");
		short_message_threshold = Integer.parseInt(properties.getProperty("word_threshold","4"));
		messages_to_wait = Integer.parseInt(properties.getProperty("messages_to_wait", "1"));
		multi_line_message_indicator = properties.getProperty("multi_line_message_indicator", "");
	}
	
	private void loadPropertieFile() {
		properties = new Properties()
		{
			@Override
			public String getProperty(String key)
			{
				String value = super.getProperty(key);
				return value;
			}
		};
			
		File propertiesFile = new File(USER_POKE_PROPERTIES_FILE);
		if(!propertiesFile.exists())
		{
			propertiesFile = new File("properties/" + USER_POKE_PROPERTIES_FILE);
		} 
		if (propertiesFile.exists()) try
		{
			properties.load(new FileReader(propertiesFile));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return;
		}
	}
	
	public boolean multiMessageActive() {
		return !multi_message_response_array.isEmpty() ? true : false;
	}
	
    private boolean checkMultiMessage(String text) {
    	if(multi_line_message_indicator.length() > 0 && text.length() > multi_line_message_indicator.length()) {
    		return text.substring(text.length() - multi_line_message_indicator.length()).contentEquals(multi_line_message_indicator);
    	}
    	return false;
    }
    
    public void handleUserMessage(Event event) {
    	if(event instanceof MessageEvent) {
    		messages.add((MessageEvent) event);
    		handleMultiLineMessage((MessageEvent) event);
    	}
    }
    
    private void handleMultiLineMessage(MessageEvent event) {
    	String message = event.getText().trim();
    	if(checkMultiMessage(message)) {
    		multi_message_response_array.add(event);
    	} else if(multi_message_response_array.size() > 0){
    		
    		Iterator<MessageEvent> iter = multi_message_response_array.iterator(); 
    		String concatenatedMessage =  iter.next().getText();
    		while (iter.hasNext()) {
    			concatenatedMessage += " | " + iter.next().getText();
    		}
    		
    		event.setText(concatenatedMessage + " | " + message);
    		multi_message_response_array.clear();
    	}
        
    }
    
    private boolean checkForToShortAnswers() {
    	if(!user_poke_message.equals("") && messages.size() > short_message_threshold && userPoked == false) {
    		Iterator<MessageEvent> iter = messages.iterator(); 
    		for(int count = 0; count < messages_to_wait; count++) 
    			iter.next(); // skip messages which should get ignored
    		int word_count = 0;
    		while (iter.hasNext()) {
    			word_count += countWordsofString(iter.next().getText());
    		}
    		float average_words_per_message = word_count / (messages.size() - messages_to_wait);
    		
    		if(average_words_per_message < short_message_threshold) {
    			userPoked = true;
    			return true;
    		}
    	} 
    	return false;
    }
    
    public void handleToShortMessages(List<String> tutorTurns) {
    	if(checkForToShortAnswers()) {
    		tutorTurns.add(0, user_poke_message);
    	}
    }
    
    private int countWordsofString(String input) {
        if (input == null || input.isEmpty()) {
          return 0;
        }

        String[] words = input.split("\\s+");
        return words.length;
    }
    
    
}
