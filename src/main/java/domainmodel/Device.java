package domainmodel;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "room")
public class Device {

    private String serialNumber;
    private String room;
    private Integer limit = 20;
}
