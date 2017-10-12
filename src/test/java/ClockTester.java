import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by SimplyBallistic on 11/10/2017.
 *
 * @author SimplyBallistic
 */
public class ClockTester {
    @Test
    public void shouldGetRightTime() {
        long time = 5400000;
        long time2 = 3700000;
        long min = TimeUnit.MILLISECONDS.toMinutes(time);
        long hrs = TimeUnit.MILLISECONDS.toHours(time);
        System.out.println(hrs + " hours and " + (min - hrs * 60) + " min");
    }
}
