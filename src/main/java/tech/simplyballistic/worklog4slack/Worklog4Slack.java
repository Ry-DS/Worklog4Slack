package tech.simplyballistic.worklog4slack;

import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


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


        if (session == null) return;
        timeManager = new TimeManager(this);
        session.addMessagePostedListener(new CommandHandler(timeManager));
        session.addPresenceChangeListener((presenceChange, slackSession) -> {
            SlackUser slackUser=slackSession.findUserById(presenceChange.getUserId());
            if(slackUser!=null&&presenceChange.getPresence().equals(SlackPersona.SlackPresence.AWAY)&&timeManager.isOnClock(slackUser)){
                slackSession.sendMessageToUser(slackUser,new SlackPreparedMessage.Builder().withMessage("It seems you have gone offline while on the clock! Make sure to get online or else you will " +
                        "be off the clock in *5 minutes!*").withLinkNames(true).build());

            }
        });


    }

    private void loadLogger() {
        logger=Logger.getLogger("Worklog4Slack");
        for(Handler iHandler:logger.getParent().getHandlers()) {
            logger.getParent().removeHandler(iHandler);
        }

        ConsoleHandler handler=new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter(){
            @Override
            public String format(LogRecord record) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(record.getMillis());
                Date date = new Date(record.getMillis());
                SimpleDateFormat format = new SimpleDateFormat();


                return format.format(date) + ": " + record.getMessage() + "\n";
            }
        });
        logger.addHandler(handler);

    }

    private void initSession() {
        BufferedReader reader=null;

        try{
            reader=new BufferedReader(new FileReader(new File(System.getProperty("user.dir"),"slack.token")));
            String token=reader.readLine();
            logger.info("Using token: "+token);
            session = SlackSessionFactory.createWebSocketSlackSession(token);
            session.connect();

        }catch(IOException|IllegalArgumentException e){
            logger.severe("Failed to connect to Slack! Did you make a file named 'slack.token' with your token?");
            logger.severe(e.getMessage());
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


    public static void main(String[] args){
        new Worklog4Slack();

    }

}
