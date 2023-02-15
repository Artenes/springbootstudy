package degallant.github.io.todoapp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
@Setter
@Getter
public class OffsetHolder {

    private ZoneOffset offset;

    public OffsetDateTime applyTo(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.withOffsetSameInstant(offset != null ? offset : OffsetDateTime.now().getOffset());
    }

}
