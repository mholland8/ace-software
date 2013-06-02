import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.useful.jdbc.DBReader;

public class Main implements MessageListener
{

    private static final Logger logger = LogManager.getLogger(Main.class);

    /**
     * @param args
     */
    public static void main(final String[] args)
    {
        logger.info("Startup...");

        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("beans.xml");

        logger.info("Spring Container initialised");

        DBReader reader = (DBReader) appContext.getBean("dbReader");
        reader.runQuery();

        appContext.close();
    }

    public void onMessage(Message arg0)
    {
        if (arg0 instanceof TextMessage)
        {
            TextMessage tm = (TextMessage) arg0;
            try
            {
                logger.info(tm.getText());
            }
            catch (JMSException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            logger.warn("received unknown message type");
        }
    }
}
