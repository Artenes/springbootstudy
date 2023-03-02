package degallant.github.io.todoapp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Setter
@Getter
@ToString
@Component
public class OffsetHolder {

    private ZoneOffset offset;

    public OffsetDateTime applyTo(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.withOffsetSameInstant(offset != null ? offset : OffsetDateTime.now().getOffset());
    }

}

