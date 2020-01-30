package basilica2.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.ArrayList;
import java.util.Map;
// import java.sql.Connection;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import edu.cmu.cs.lti.basilica2.core.Component;
import basilica2.agents.components.ChatClient;
import basilica2.agents.events.EchoEvent;
import basilica2.agents.events.MessageEvent;
import basilica2.agents.events.PresenceEvent;
import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.basilica2.core.Event;
import edu.cmu.cs.lti.project911.utils.log.Logger;

public class ActiveMQClient extends Component implements ChatClient
{	
    private ConnectionFactory factory;
    private Connection connection = null;
    private String uri;
    private Session session;

    private ArrayList<MessageConsumer> consumers;
    
    @Override
	public void disconnect()
	{
		System.out.println("ActiveMQ client disconnecting...");
		
	}

    public ActiveMQClient(Agent a, String n, String pf)
	{
        this(a,n,pf,61616);
    }

    public ActiveMQClient(Agent a, String n, String pf, int port) {
        this(a,n,pf,"tcp://localhost:" + port);
    }

    public ActiveMQClient(Agent a, String n, String pf, String uri) {
		super(a, n, pf);
        this.uri = uri;
        this.consumers = new ArrayList<>();
        System.out.println("*** ActiveMQServer: initializing ***");
        initActiveMQServer();
        System.out.println("*** ActiveMQServer: initialization complete ***");
    }

    private void initActiveMQServer() {
        factory = new ActiveMQConnectionFactory(this.uri);
        try {
            connection = factory.createConnection();
            //connection.setClientID("Customer");
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribe(ISLSubscriber subscriber, String topicString) {
        try {
            for (MessageConsumer consumer: consumers) {
                TopicListener listener = (TopicListener)consumer.getMessageListener();
                if (listener.getSubscriber().hashCode() == subscriber.hashCode()
                        && listener.getTopic().getTopicName().equals(topicString)) {
                    System.out.println(subscriber.toString() + "already subscribed topic " + topicString);
                    return;
                }
            }
            Topic destination = session.createTopic(topicString);
            MessageConsumer consumer = session.createConsumer(destination);
            consumers.add(consumer);
            consumer.setMessageListener(new TopicListener(subscriber, destination));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribe(ISLSubscriber subscriber, String topicString) {
        for (MessageConsumer consumer: consumers) {
            try {
                TopicListener listener = (TopicListener)consumer.getMessageListener();
                if (listener.getSubscriber().hashCode() == subscriber.hashCode()
                    && listener.getTopic().getTopicName().equals(topicString)) {
                    consumers.remove(consumer);
                    break;
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

	@Override
	public String getType()
	{
		// TODO Auto-generated method stub
		return "Client";
	}

	@Override
	public void login(String roomName)
	{
		
	}
    
    @Override
	protected void processEvent(Event e)
	{
		if(e instanceof MessageEvent)
		{
			MessageEvent me = (MessageEvent) e;
			EchoEvent ee = new EchoEvent(e.getSender(), me);
			
			getAgent().getComponent("inputCoordinator").receiveEvent(ee);
		}

	}
}
