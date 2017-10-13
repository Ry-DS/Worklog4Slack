package tech.simplyballistic.worklog4slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
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
    Logger logger;
    private SlackSession session;
    private TimeManager timeManager;

    private Worklog4Slack() {
        loadLogger();
        initSession();


        if (session == null || !session.isConnected()) return;
        timeManager = new TimeManager(this);
        session.addMessagePostedListener(new CommandHandler(timeManager));
        session.addPresenceChangeListener(new InactivityHandler(this));
        logger.info("Startup successful!");



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


                return format.format(date) + ": [" + record.getLevel() + "] " + record.getMessage() + "\n";
            }
        });
        logger.addHandler(handler);
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

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public static void main(String[] args){
        new Worklog4Slack();

    }

}
