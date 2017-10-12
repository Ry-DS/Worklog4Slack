import com.ullink.slack.simpleslackapi.SlackUser;

/**
 * Created by SimplyBallistic on 13/10/2017.
 *
 * @author SimplyBallistic
 */
public class TestUser implements SlackUser {
    private String id, username;
    private SlackPresence presence;

    public TestUser(String id, String username, SlackPresence presence) {
        this.id = id;
        this.username = username;
        this.presence = presence;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public String getRealName() {
        return "Ryan";
    }

    @Override
    public String getUserMail() {
        return "worklog@4slack";
    }

    @Override
    public String getUserSkype() {
        return "live:ryan93_2";
    }

    @Override
    public String getUserPhone() {
        return "+61000000000";
    }

    @Override
    public String getUserTitle() {
        return "Professional";
    }

    @Override
    public boolean isDeleted() {
        return false;
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    @Override
    public boolean isOwner() {
        return false;
    }

    @Override
    public boolean isPrimaryOwner() {
        return false;
    }

    @Override
    public boolean isRestricted() {
        return false;
    }

    @Override
    public boolean isUltraRestricted() {
        return false;
    }

    @Override
    public boolean isBot() {
        return false;
    }

    @Override
    public String getTimeZone() {
        return "+10";
    }

    @Override
    public String getTimeZoneLabel() {
        return "Australia";
    }

    @Override
    public Integer getTimeZoneOffset() {
        return 10;
    }

    @Override
    public SlackPresence getPresence() {
        return SlackPresence.ACTIVE;
    }
}
