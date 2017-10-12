package tech.simplyballistic.worklog4slack;

import com.ullink.slack.simpleslackapi.SlackUser; /**
 * Created by SimplyBallistic on 10/10/2017.
 *
 * @author SimplyBallistic
 */
public class TimeManager{
    private Worklog4Slack session;

    public TimeManager(Worklog4Slack session) {
        this.session = session;
    }

    public boolean startUser(SlackUser sender) {
        return false;
    }

    public boolean isOnClock(SlackUser sender) {
        return false;
    }

    public long stopUser(SlackUser sender) {

        return 0;
    }
}
