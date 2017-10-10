package tech.simplyballistic.worklog4slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * Created by SimplyBallistic on 10/10/2017.
 *
 * @author SimplyBallistic
 */
public class Worklog4Slack {
    private Logger logger;
    public Worklog4Slack() {
        logger=Logger.getLogger("Worklog4Slack");
        try{
        SlackSession session = SlackSessionFactory.getSlackSessionBuilder("token").build();
        session.connect();
        }catch(IOException|IllegalArgumentException e){
            logger.severe("Failed to connect to Slack! Is the auth Token correct?");
            e.printStackTrace();
        }

    }

    public static void main(String[] args){
        new Worklog4Slack();

    }

}
