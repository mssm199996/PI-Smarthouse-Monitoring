package websocket;

import domainmodel.Device;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddRemoveDeviceRequest {

    private Device device;
    private boolean add;
}
