package tech.simplyballistic.worklog4slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
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
        logger=Logger.getLogger("Worklog4Slack");
        for(Handler iHandler:logger.getParent().getHandlers())
        {
            logger.getParent().removeHandler(iHandler);
        }

        ConsoleHandler handler=new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter(){
            @Override
            public synchronized String format(LogRecord record) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(record.getMillis());

                return "["+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+"]"+": "+record.getMessage()+"\n";
            }
        });
        logger.addHandler(handler);
        BufferedReader reader=null;

        try{
            reader=new BufferedReader(new FileReader(new File(System.getProperty("user.dir"),"slack.token")));
            String token=reader.readLine();
            logger.info("Using token: "+token);
        session= SlackSessionFactory.createWebSocketSlackSession(token);
        session.connect();

        }catch(IOException|IllegalArgumentException e){
            logger.severe("Failed to connect to Slack! Did you make a file named 'slack.token' with your token?");
            e.printStackTrace();
            return;
        }
        finally {
            if(reader!=null)
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(session==null)return;
        timeManager=new TimeManager(this);
        session.addMessagePostedListener((message,session)->{
            //logger.info(message.getMessageContent()+" posted by "+message.getSender().getUserName());
            if(message.getMessageContent().toLowerCase().contains("hello")){
                session.sendMessage(message.getChannel(),"Hi there! I am Worklogger4Slack! Type -help for help");

            }
            switch (message.getMessageContent()){
                case "-help":

                    session.sendMessage(message.getChannel(),"`Help for this bot`");
                    session.sendMessage(message.getChannel(),"*-start* starts your clock :clock4:" +
                            "\n *-stop* stops your clock :alarm_clock:"+
                            "\n *-time <user> <tday,week,all>* views a user's time committed :timer_clock:"+
                            "\n *-onclock* views users currently on the clock :arrows_clockwise:"+
                            "\n *-most <tday,week,all>* views user leaderboard for clocked hours :trophy:");
                    break;
                case "-start":
                    if(!timeManager.startUser(message.getSender())){
                        session.sendMessage(message.getChannel(),"**You are already on the clock!**");
                    }else session.sendMessage(message.getChannel(),"You are not now on the clock! :clock12:");
                    break;
                    //TODO command manager if we make more

            }



        });



    }

    public SlackSession getSession() {
        return session;
    }

    public static void main(String[] args){
        new Worklog4Slack();

    }

}
