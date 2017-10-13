package tech.simplyballistic.worklog4slack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by SimplyBallistic on 10/10/2017.
 *
 * @author SimplyBallistic
 */
public class TimeManager{
    private Worklog4Slack session;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File folder = new File(System.getProperty("user.dir"), "database");
    private Map<String, UserData> data = new HashMap<>();


    public TimeManager(Worklog4Slack session) {
        this.session = session;


    }

    public boolean startUser(SlackUser sender) {
        load(sender);
        if (data.get(sender.getId()).activeSession == null) {
            data.get(sender.getId()).activeSession = new Session();
            save(sender);
            return true;


        }

        return false;


    }


    public boolean isOnClock(SlackUser sender) {
        load(sender);
        return data.get(sender.getId()).activeSession != null;
    }

    public long stopUser(SlackUser sender) {
        load(sender);
        if (data.get(sender.getId()).activeSession == null)
            return -1;
        UserData userData = data.get(sender.getId());
        userData.activeSession.stop = new Date();
        userData.workedSessions.add(new Session(userData.activeSession));
        Session session = userData.activeSession;
        userData.activeSession = null;
        save(sender);
        return getDateDiff(session.start, session.stop, TimeUnit.MILLISECONDS);
    }

    public Collection<String> getOnClock() {
        Set<String> onClock = new HashSet<>();
        data.forEach((s, userData) -> {
            if (userData.activeSession != null)
                onClock.add(userData.name);
        });
        return onClock;
    }

    private void save(SlackUser user) {
        try {
            folder.mkdir();
            FileWriter writer = new FileWriter(new File(folder, user.getId() + ".json"));
            writer.write(gson.toJson(data.get(user.getId())));
            writer.flush();
            writer.close();
            load(user);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void load(SlackUser user) {

        if (!data.containsKey(user.getId()))
            try {
                data.put(user.getId(), gson.fromJson(new FileReader(new File(folder, user.getId() + ".json")), UserData.class));
                if (data.get(user.getId()) == null)
                    data.put(user.getId(), new UserData(user.getUserName()));
                data.get(user.getId()).name = user.getUserName();
                if (data.get(user.getId()).workedSessions.size() >= 100)
                    data.get(user.getId()).workedSessions.clear();
            } catch (FileNotFoundException e) {
                data.put(user.getId(), new UserData(user.getUserName()));
            }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        return timeUnit.convert(date2.getTime() - date1.getTime(), TimeUnit.MILLISECONDS);
    }

    public long getTotalTime(SlackUser user, TimeType type, TimeUnit unit) {
        load(user);
        if (type == TimeType.CURRENT && !isOnClock(user)) {
            return -1;
        }
        if (type == TimeType.CURRENT) {
            return getDateDiff(data.get(user.getId()).activeSession.start, new Date(), unit);
        }
        Collection<Session> sessions = data.get(user.getId()).workedSessions.stream().filter(session -> {
            if (session.stop == null)
                return false;
            if (type == TimeType.ALL)
                return true;
            int timeType = Calendar.DAY_OF_YEAR;
            if (type == TimeType.TDAY) {
                timeType = Calendar.DAY_OF_YEAR;

            } else if (type == TimeType.WEEK) {
                timeType = Calendar.WEEK_OF_YEAR;


            }
            Calendar calender = Calendar.getInstance();
            calender.setTime(session.stop);
            return calender.get(timeType) == Calendar.getInstance().get(timeType) && calender.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);

        }).collect(Collectors.toList());
        if (sessions.size() == 0)
            return -1;
        long time = 0;
        for (Session session : sessions) {
            if (session.stop != null)
                time += getDateDiff(session.start, session.stop, unit);


        }
        return time;
    }
}
