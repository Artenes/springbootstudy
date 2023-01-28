package degallant.github.io.todoapp.common;

import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

/**
 * @noinspection ClassCanBeRecord
 */
@RequiredArgsConstructor
public class Time {

    private final OffsetDateTime now;

    public static Time from(String yearMonthDayString) {
        String[] parts = yearMonthDayString.split("-");
        var year = Integer.parseInt(parts[0]);
        var month = Integer.parseInt(parts[1]);
        var day = Integer.parseInt(parts[2]);
        var zone = OffsetDateTime.now().getOffset();
        var now = OffsetDateTime.of(year, month, day, 0, 0, 0, 0, zone);
        return new Time(now);
    }

    public OffsetDateTime startOfDay() {
        var year = now.getYear();
        var month = now.getMonthValue();
        var day = now.getDayOfMonth();
        var zone = now.getOffset();
        return OffsetDateTime.of(year, month, day, 0, 0, 0, 0, zone);
    }

    public OffsetDateTime endOfDay() {
        var year = now.getYear();
        var month = now.getMonthValue();
        var day = now.getDayOfMonth();
        var zone = now.getOffset();
        return OffsetDateTime.of(year, month, day, 23, 59, 59, 0, zone);
    }

}
