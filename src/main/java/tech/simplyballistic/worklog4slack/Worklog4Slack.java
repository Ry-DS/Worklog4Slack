package tech.simplyballistic.worklog4slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;


/**
 * Created by SimplyBallistic on 10/10/2017.
 *
 * @author SimplyBallistic
 */
public class Worklog4Slack {
    private Logger logger;
    private SlackSession session;
    private TimeManager timeManager;

    private Worklog4Slack() {
        loadLogger();
        initSession();


        if (session == null || !session.isConnected()) return;
        timeManager = new TimeManager(this);
        session.addMessagePostedListener(new CommandHandler(this));
        session.addPresenceChangeListener(new InactivityHandler(this));
        logger.info("Startup successful!");



    }

    private void loadLogger() {
        logger = LoggerFactory.getLogger("Worklog4Slack");

        logger.info("Worklogger4Slack: By Ryan Samarakoon");
        System.setOut(new PrintStream(System.out) {
            @Override
            public void println(Object x) {
                logger.info(x.toString().replace("\n", ""));
            }

            @Override
            public void println(String s) {
                logger.info(s.replace("\n", ""));
            }
        });

    }

    private void initSession() {
        BufferedReader reader=null;
        logger.info("Attempting connection to Slack...");
        try{
            reader=new BufferedReader(new FileReader(new File(System.getProperty("user.dir"),"slack.token")));
            String token=reader.readLine();
            logger.info("Using token: "+token);
            session = SlackSessionFactory.createWebSocketSlackSession(token);
            session.connect();


        }catch(IOException|IllegalArgumentException e){
            logger.error("Failed to connect to Slack! Did you make a file named 'slack.token' with your token?", e);
        } finally {

            try {
                if (reader != null)
                    reader.close();
                if (new File(System.getProperty("user.dir"), "slack.token").createNewFile()) {
                    logger.info("Created a new slack.token file! Open this file in your text editor and paste your slack token.");
                    logger.info("You can learn more at api.slack.com");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public SlackSession getSession() {
        return session;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    Logger getLogger() {
        return logger;
    }

    public static void main(String[] args){
        new Worklog4Slack();

    }

}
