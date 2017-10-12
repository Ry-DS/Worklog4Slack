import com.google.gson.Gson;
import com.ullink.slack.simpleslackapi.SlackPersona;
import org.junit.Assert;
import org.junit.Test;
import tech.simplyballistic.worklog4slack.TimeManager;
import tech.simplyballistic.worklog4slack.UserData;

import java.util.concurrent.TimeUnit;

/**
 * Created by SimplyBallistic on 11/10/2017.
 *
 * @author SimplyBallistic
 */
public class ClockTester {
    private final TestUser user = new TestUser("2k17", "worklog", SlackPersona.SlackPresence.ACTIVE);

    @Test
    public void shouldGetRightTime() {
        long time = 5400000;
        long time2 = 3700000;
        long min = TimeUnit.MILLISECONDS.toMinutes(time);
        long hrs = TimeUnit.MILLISECONDS.toHours(time);
        System.out.println(hrs + " hours and " + (min - hrs * 60) + " min");
    }

    @Test
    public void shouldSaveTimeData() {
        TimeManager manager = new TimeManager(null);
        if (manager.isOnClock(user))
            manager.stopUser(user);
        assert manager.startUser(user);
        assert manager.isOnClock(user);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(manager.stopUser(user));


    }

    @Test
    public void shouldSaveActiveSession() {
        TimeManager manager = new TimeManager(null);
        assert manager.startUser(user);


    }

    @Test
    public void shouldDeserializeDate() {
        Gson gson = new Gson();
        UserData data = gson.fromJson("{\n" +
                "  \"workedSessions\": [\n" +
                "    {\n" +
                "      \"start\": \"Oct 13, 2017 1:30:19 AM\",\n" +
                "      \"stop\": \"Oct 13, 2017 1:30:20 AM\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"activeSession\": {\n" +
                "    \"start\": \"Oct 13, 2017 1:28:59 AM\"\n" +
                "  }\n" +
                "}", UserData.class);
        Assert.assertEquals("Fri Oct 13 01:28:59 AEDT 2017", data.activeSession.start.toString());
        Assert.assertEquals("Fri Oct 13 01:30:19 AEDT 2017", data.workedSessions.get(0).start.toString());

    }
}
