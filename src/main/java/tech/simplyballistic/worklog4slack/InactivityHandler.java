package tech.simplyballistic.worklog4slack;

import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.PresenceChange;
import com.ullink.slack.simpleslackapi.listeners.PresenceChangeListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by SimplyBallistic on 13/10/2017.
 *
 * @author SimplyBallistic
 */
public class InactivityHandler extends Thread implements PresenceChangeListener {
    public Map<String, Long> inactiveUsers = new HashMap<>();
    private Worklog4Slack session;
    private boolean run = true;

    public InactivityHandler(Worklog4Slack session) {
        super("InactivityChecker");
        this.session = session;

        start();
    }

    @Override
    public void run() {
        while (run) {
            Iterator<Map.Entry<String, Long>> iterator = inactiveUsers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                SlackUser slackUser = session.getSession().findUserById(entry.getKey());

                if (slackUser == null || !session.getTimeManager().isOnClock(slackUser)) {
                    iterator.remove();
                    continue;
                }
                if (System.currentTimeMillis() - entry.getValue() > 5 * 60 * 1000) {
                    long time = session.getTimeManager().stopUser(slackUser);
                    long min = TimeUnit.MILLISECONDS.toMinutes(time);
                    long hrs = TimeUnit.MILLISECONDS.toHours(time);
                    session.getLogger().info("User " + slackUser.getRealName() + " off clock due to inactivity after " + min + " min");
                    session.getSession().getChannels().forEach(channel -> {
                        if (channel.isDirect()) return;



                        session.getSession().sendMessage(channel, "<@" + entry.getKey() + "> was taken off the clock due to inactivity after `" + hrs + " hours and " + (min - hrs * 60) + " min`");
                    });


                }

            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onEvent(PresenceChange presenceChange, SlackSession slackSession) {

        SlackUser slackUser = slackSession.findUserById(presenceChange.getUserId());

        if (slackUser == null || slackUser.isBot())
            return;
        if (!session.getTimeManager().isOnClock(slackUser))
            return;
        if (presenceChange.getPresence() == SlackPersona.SlackPresence.AWAY) {
            session.getLogger().info("User " + slackUser.getRealName() + " away on clock");
            slackSession.sendMessageToUser(slackUser, new SlackPreparedMessage.Builder().withMessage("It seems you have gone offline while on the clock! Make sure to get online or else you will " +
                    "be off the clock in *5 minutes!* <@" + slackUser.getId() + ">").withLinkNames(true).build());
            inactiveUsers.put(slackUser.getId(), System.currentTimeMillis());

        } else if (presenceChange.getPresence() == SlackPersona.SlackPresence.ACTIVE && inactiveUsers.containsKey(slackUser.getId())) {
            session.getLogger().info("User " + slackUser.getRealName() + " returned on clock");
            slackSession.sendMessageToUser(slackUser, "Thanks for jumping online! You will not be taken off the clock", null);
            inactiveUsers.remove(slackUser.getId());
        }

    }
}
