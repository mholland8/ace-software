import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class Main implements MessageListener {

	private static final Logger logger = LogManager.getLogger(Main.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.info("Startup...");
		
		ApplicationContext appContext = new ClassPathXmlApplicationContext("beans.xml");
	}
	public void onMessage(Message arg0) {
		logger.info(arg0);
		
	}

}
