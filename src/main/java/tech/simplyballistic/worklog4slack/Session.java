package tech.simplyballistic.worklog4slack;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by SimplyBallistic on 13/10/2017.
 *
 * @author SimplyBallistic
 */
public class Session implements Serializable {
    public Date start;
    public Date stop;

    public Session(Date start, Date stop) {
        this.start = start;
        this.stop = stop;
    }

    public Session(Date start) {
        this.start = start;

    }

    public Session() {
        start = new Date();
    }

    public Session(Session session) {
        start = new Date(session.start.getTime());
        stop = new Date(session.stop.getTime());
    }
}
