package nl.rivm.uvsg.mockedclock;

import org.springframework.context.annotation.Bean;

import java.time.*;
import java.time.temporal.ChronoUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockedClockFactory {

    public static final int YEAR = 2025;
    public static final Month MONTH = Month.FEBRUARY;
    public static final int DAY = 14;
    public static final int HOUR = 16;
    public static final int MINUTE = 15;

    @Bean
    public Clock getClock() {

        Clock mockedClock = mock(Clock.class);
        Instant now = Instant.now();

        when(mockedClock.instant()).thenAnswer(invocation -> {
            Duration offset = getOffset(now, YEAR, MONTH, DAY, HOUR, MINUTE);
            return Instant.now().minus(offset);
        });
        return mockedClock;
    }

    private static Duration getOffset(Instant now, int year, Month month, int day, int hour, int minute) {

        LocalDate fixedDate = LocalDate.of(year, month, day);
        Instant fixedInstant = fixedDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant nowTruncated = now.truncatedTo(ChronoUnit.MINUTES).minus(hour, ChronoUnit.HOURS).minus(minute, ChronoUnit.MINUTES);

        return Duration.between(fixedInstant, nowTruncated);
    }
}
