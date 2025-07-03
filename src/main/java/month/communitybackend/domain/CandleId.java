package month.communitybackend.domain;

import lombok.*;
import jakarta.persistence.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class CandleId implements Serializable {
    private String market;
    private Long openTime;
}