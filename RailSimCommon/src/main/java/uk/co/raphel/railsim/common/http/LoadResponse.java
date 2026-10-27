package uk.co.raphel.railsim.common.http;


import lombok.Getter;
import lombok.Setter;

@Deprecated
@Getter @Setter
public class LoadResponse{
private Boolean success;
private String errorMessage;


    public LoadResponse(Boolean status, String errorMessage) {
        this.success = status ;
        this.errorMessage = errorMessage;
    }


}