package uk.co.raphel.trainsimserver.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j(topic = "ProfileService")
public class ProfileService {


    @Value("${spring.profiles.active:default}")
    private String activeProfiles;
    private List<String> profilesActive;


    @PostConstruct()
    public void init() {
        if (activeProfiles == null || activeProfiles.isEmpty()) {
            profilesActive = new ArrayList<>();
            profilesActive.add("default");
        } else {
            profilesActive = Arrays.stream(activeProfiles.split(",")).toList();
        }
    }

    public boolean isProfileActive(String request) {
        return profilesActive.stream().anyMatch(p -> p.equalsIgnoreCase(request));
    }
}


