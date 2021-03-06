package basilica2.agents.listeners;

import edu.cmu.cs.lti.basilica2.core.Event;

import java.util.Map;

import basilica2.agents.listeners.BasilicaPreProcessor;
import basilica2.agents.components.InputCoordinator;
import basilica2.agents.events.LaunchEvent;
import basilica2.agents.events.MessageEvent;
import basilica2.agents.events.PresenceEvent;
import basilica2.agents.listeners.PresenceWatcher;
import edu.cmu.cs.lti.basilica2.core.Component;
import edu.cmu.cs.lti.basilica2.core.Event;
import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.basilica2.core.Component;
import basilica2.agents.components.StateMemory;
import basilica2.agents.data.State;
import edu.cmu.cs.lti.project911.utils.log.Logger;
import edu.cmu.cs.lti.project911.utils.time.TimeoutReceiver;
import edu.cmu.cs.lti.project911.utils.time.Timer;

import java.util.Hashtable;
import java.util.Map;


public class MultiModalFilter extends BasilicaAdapter
{ 

	public static String GENERIC_NAME = "MultiModalFilter";
	public static String GENERIC_TYPE = "Filter";
	protected enum multiModalTag  
	{
		multimodal, identity, speech, location, facialExp, bodyPos, emotion;
	}
	private String multiModalDelim = ";%;";
	private String withinModeDelim = ":";	
	private Map<String, String> locations;
	private boolean shouldTrackLocation = true;
	private InputCoordinator source;
	private String status = "";
	private boolean isTrackingLocation = false;

	public MultiModalFilter(Agent a) 
	{
		super(a);
		locations = new Hashtable<String,String>();
	}

	public void setTrackMode(boolean m)
	{
		shouldTrackLocation = m;
	}

	public String getStatus()
	{
		return status;
	}

	private void handleLaunchEvent(LaunchEvent le)
	{
//		startLocationTracking();
	}

	@Override
	public void preProcessEvent(InputCoordinator source, Event e)
	{
		if (e instanceof MessageEvent)
		{
			handleMessageEvent(source, (MessageEvent) e);
		}
	}

	private void handleMessageEvent(InputCoordinator source, MessageEvent me)
	{
		String text = me.getText();
		String[] multiModalMessage = text.split(multiModalDelim);
		if (multiModalMessage.length > 1) {

			for (int i = 0; i < multiModalMessage.length; i++) {
				System.out.println("=====" + " Multimodal message entry -- " + multiModalMessage[i] + "======");
				String[] messagePart = multiModalMessage[i].split(withinModeDelim,2);
				
				multiModalTag tag = multiModalTag.valueOf(messagePart[0]);
				
				switch (tag) {
				case multimodal:
					break;
				case identity:
					// System.out.println("Identity: " + messagePart[1]);
					me.setFrom(messagePart[1]);
					checkPresence(source,me);
					break;
				case speech:
					// System.out.println("Speech: " + messagePart[1]);
					me.setText(messagePart[1]);
					break;
				case location:
					// System.out.println("Location: " + messagePart[1]);
					if (shouldTrackLocation)
						updateLocation(me,messagePart[1]);
					break;
				case facialExp:
					// System.out.println("Facial expression: " + messagePart[1]);
					break;
				case bodyPos:
					// System.out.println("Body position: " + messagePart[1]);
					break;
				case emotion:
					// System.out.println("Emotion: " + messagePart[1]);
					break;
				default:
					System.out.println(">>>>>>>>> Invalid multimodal tag: " + messagePart[0] + "<<<<<<<<<<");
				}
			}}    // Don't know why two "}" are required here 
    }
		

	private void checkPresence(InputCoordinator source, MessageEvent me) {
		String identity = me.getFrom();
        State state = State.copy(StateMemory.getSharedState(agent));
        State.Student user = state.getStudentById(identity);
        if (user == null) {
        	PresenceEvent pe = new PresenceEvent(source,identity,PresenceEvent.PRESENT);
        	handlePresenceEvent(source,pe);
        }     	
	}
	
	private void handlePresenceEvent(final InputCoordinator source, PresenceEvent pe)
	{
		State olds = StateMemory.getSharedState(agent);
		State news;
		if (pe.getType().equals(PresenceEvent.PRESENT))
		{
			if (olds != null)
			{
				news = State.copy(olds);
			}
			else
			{
				news = new State();
			}
			news.addStudent(pe.getUsername());
			Logger.commonLog(getClass().getSimpleName(),Logger.LOG_NORMAL,"STUDENTS COUNT: " + news.getStudentCount());
			StateMemory.commitSharedState(news, agent);

		}
		else if (pe.getType().equals(PresenceEvent.ABSENT))
		{
			State updateState = State.copy(olds);
			updateState.removeStudent(pe.getUsername());
			StateMemory.commitSharedState(updateState, agent);
		}
	}

	private void updateLocation(MessageEvent me, String location)
	{
		
		String identity = me.getFrom();

        State s = State.copy(StateMemory.getSharedState(agent));
        s.setLocation(identity, location);
        StateMemory.commitSharedState(s, agent);
	}
	

	/**
	 * @return the classes of events that this Preprocessor cares about
	 */
	@Override
	public Class[] getPreprocessorEventClasses()
	{
		//only MessageEvents will be delivered to this watcher.
		return new Class[]{MessageEvent.class};
	}


	@Override
	public void processEvent(InputCoordinator source, Event event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Class[] getListenerEventClasses() {
		// TODO Auto-generated method stub
		return null;
	}

}
