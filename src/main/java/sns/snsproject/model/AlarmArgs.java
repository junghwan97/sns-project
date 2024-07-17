package sns.snsproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlarmArgs {

    private Long fromUserId;
    private Long targetId;
}
