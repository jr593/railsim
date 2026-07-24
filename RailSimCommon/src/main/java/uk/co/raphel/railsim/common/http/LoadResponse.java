package uk.co.raphel.railsim.common.http;

public class LoadResponse{
private Boolean success;
private String errorMessage;


    public LoadResponse(Boolean status, String errorMessage) {
        this.success = status ;
        this.errorMessage = errorMessage;
    }


}