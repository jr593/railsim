package uk.co.raphel.railsim.common.http;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.co.raphel.railsim.common.dto.RunningService;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class TrainsResponse {
private Boolean success;
private String errorMessage;
private List<RunningService> trains;

    public TrainsResponse(Boolean status, String errorMessage, List<RunningService> trains) {
        this.success = status ;
        this.errorMessage = errorMessage;
        this.trains = trains;
    }


}