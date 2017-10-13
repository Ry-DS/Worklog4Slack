package tech.simplyballistic.worklog4slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by SimplyBallistic on 13/10/2017.
 *
 * @author SimplyBallistic
 */
public class CommandHandler implements SlackMessagePostedListener {
    private TimeManager timeManager;

    public CommandHandler(TimeManager timeManager) {
        this.timeManager = timeManager;
    }

    @Override
    public void onEvent(SlackMessagePosted message, SlackSession session) {
        //logger.info(message.getMessageContent()+" posted by "+message.getSender().getUserName());
        if (message.getMessageContent().toLowerCase().contains("hello") || message.getMessageContent().contains("<@" + session.sessionPersona().getId() + ">")) {
            session.sendMessage(message.getChannel(), "Hi there! I am Worklogger4Slack! Type -help for help");

        }
        switch (message.getMessageContent().split(" ")[0]) {
            case "-help":

                session.sendMessage(message.getChannel(), "`Help for this bot`");
                session.sendMessage(message.getChannel(), "*-start* starts your clock :clock4:" +
                        "\n *-stop* stops your clock :alarm_clock:" +
                        "\n *-time <user> <tday,week,month,all,current>* views a user's time committed :timer_clock:. You can add 'last' at the end to view times for last day/week/month" +
                        "\n *-onclock* views users currently on the clock :arrows_clockwise:" +
                        "\n *-top <tday,week,month,all>* views user leaderboard for clocked hours :trophy:");
                break;
            case "-start":
                if (!timeManager.startUser(message.getSender())) {
                    session.sendMessage(message.getChannel(), "*You are already on the clock!*");
                } else session.sendMessage(message.getChannel(), "You are now on the clock! :clock12:");
                break;
            case "-stop":
                if (!timeManager.isOnClock(message.getSender())) {
                    session.sendMessage(message.getChannel(), "*You are not on the clock!*");
                } else {
                    long time = timeManager.stopUser(message.getSender());
                    long min = TimeUnit.MILLISECONDS.toMinutes(time);
                    long hrs = TimeUnit.MILLISECONDS.toHours(time);
                    session.sendMessage(message.getChannel(), "Stopped. You worked for " + hrs + " hours and " + (min - hrs * 60) + " min");

                }
                break;
            case "-onclock":
                Collection<String> onClock = timeManager.getOnClock();
                session.sendMessage(message.getChannel(), "`There are " + onClock.size() + " people on the clock`");

                if (onClock.size() > 0) {
                    StringBuilder builder = new StringBuilder("Users on clock: ");
                    onClock.forEach(s -> builder.append(s).append(onClock.size() == 1 ? "" : " - "));
                    session.sendMessage(message.getChannel(), builder.toString());

                }
                break;
            case "-time":
                try {
                    String[] args = message.getMessageContent().split(" ");
                    if (args.length < 3) {
                        session.sendMessage(message.getChannel(), "Not enough args! You need to provide a user and a timetype. Do *-help* for details");
                        return;
                    }
                    TimeType type = TimeType.valueOf(args[2].toUpperCase());
                    final Pattern pattern = Pattern.compile("<@(.+?)>");
                    final Matcher matcher = pattern.matcher(args[1]);
                    if (!matcher.find()) {
                        session.sendMessage(message.getChannel(), "Invalid user provided! You must tag them.");
                        return;
                    }

                    SlackUser user = session.findUserById(matcher.group(1));

                    if (user == null) {
                        session.sendMessage(message.getChannel(), "Invalid user provided!");
                        return;
                    }
                    if (type == TimeType.CURRENT && !timeManager.isOnClock(user)) {
                        session.sendMessage(message.getChannel(), "User isn't on the clock to retrieve current time!");
                        return;
                    }
                    long time = timeManager.getTotalTime(user, type, TimeUnit.MILLISECONDS, args.length > 3 && args[3].equalsIgnoreCase("last"));
                    long min = TimeUnit.MILLISECONDS.toMinutes(time);
                    long hrs = TimeUnit.MILLISECONDS.toHours(time);
                    session.sendMessage(message.getChannel(), user.getRealName() + " has worked for `" + hrs + " hours and " + (min - hrs * 60) + " min` during that time frame");


                } catch (IllegalArgumentException e) {
                    session.sendMessage(message.getChannel(), "Invalid time type! Do *-help* for an overview");

                }
                break;
            case "-reload":
                timeManager.reload();
                session.sendMessage(message.getChannel(), "Reloaded the database :file_cabinet:");
                break;
            case "-top":
                String[] args = message.getMessageContent().split(" ");
                if (args.length < 2) {
                    session.sendMessage(message.getChannel(), "Not enough args! You need to provide a user and a timetype. Do *-help* for details");
                    return;
                }
                try {
                    List<SlackUser> top = new ArrayList<>();
                    TimeType type = TimeType.valueOf(args[1].toUpperCase());
                    boolean last = args.length > 2 && args[2].equalsIgnoreCase("last");
                    for (SlackUser slackUser : session.getUsers()) {
                        if (timeManager.data.containsKey(slackUser.getId()) && timeManager.getTotalTime(slackUser, type, TimeUnit.MILLISECONDS, last) > 0)
                            top.add(slackUser);
                    }
                    if (top.size() == 0) {
                        session.sendMessage(message.getChannel(), "No users for that time type! :open_mouth:");
                        return;
                    }
                    top.sort((Comparator.comparingLong(user -> timeManager.getTotalTime(user, type, TimeUnit.MILLISECONDS, last))));
                    Collections.reverse(top);
                    long time = timeManager.getTotalTime(top.get(0), type, TimeUnit.MILLISECONDS, args.length > 3 && args[3].equalsIgnoreCase("last"));
                    long min = TimeUnit.MILLISECONDS.toMinutes(time);
                    long hrs = TimeUnit.MILLISECONDS.toHours(time);
                    session.sendMessage(message.getChannel(), "`Leaderboard for timetype: " + args[1] + ". " + (last ? "Results are set to last" : "") + "`");
                    session.sendMessage(message.getChannel(), ":trophy: Top user: *" + top.get(0).getRealName() + "* :trophy: `" + hrs + " hrs " + (min - hrs * 60) + " min`");
                    top.remove(0);

                    for (int i = 0; i < top.size() && i < 5; i++) {
                        time = timeManager.getTotalTime(top.get(i), type, TimeUnit.MILLISECONDS, args.length > 3 && args[3].equalsIgnoreCase("last"));
                        min = TimeUnit.MILLISECONDS.toMinutes(time);
                        hrs = TimeUnit.MILLISECONDS.toHours(time);
                        session.sendMessage(message.getChannel(), i + 2 + ": *" + top.get(i).getRealName() + "* `" + hrs + " hrs " + (min - hrs * 60) + " min`");
                    }


                } catch (IllegalArgumentException e) {
                    session.sendMessage(message.getChannel(), "Invalid time type! Do *-help* for an overview");
                    e.printStackTrace();
                }
                break;
            //TODO command manager if we make more
            default:
                if (message.getMessageContent().startsWith("-"))
                    session.sendMessage(message.getChannel(), "Invalid command! That command doesn't exist or isn't yet implemented");


        }


    }
}
