package degallant.github.io.todoapp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
@Setter
@Getter
public class OffsetHolder {

    private ZoneOffset offset;

}
