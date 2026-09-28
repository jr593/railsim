package uk.co.raphel.trainsimserver.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.dto.LiveBerth;
import uk.co.raphel.railsim.common.dto.RunningRouteStop;
import uk.co.raphel.railsim.common.entity.Berth;
import uk.co.raphel.trainsimserver.repository.BerthRepository;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/*
 Responsible for keeping record of berth occupation.

 */
@Service
@Slf4j(topic="TrackManager")
public class TrackManagerService {

    private Map<Long, LiveBerth> trackMap;
    private final BerthRepository berthRepository;

    public TrackManagerService(BerthRepository berthRepository) {

        this.berthRepository = berthRepository;
    }

    @PostConstruct
    public void init() {
        trackMap = berthRepository.findAllWithPathTo().stream()
                .collect(Collectors.toMap(
                        Berth::getBerthId,
                        this::berthToLiveBerth
                ));
    }

    private LiveBerth berthToLiveBerth(Berth b) {
        LiveBerth ret = new LiveBerth();
        ret.setBerthId(b.getBerthId());
        ret.setBerthName(b.getBerthName());
        ret.setDirection(b.getDirection());
        ret.setLengthMiles(b.getLengthMiles());
        ret.setMultiOccupancy(b.getMultiOccupancy());
        ret.setMilesFromOrigin(b.getMilesFromOrigin());
        ret.setPathTo(b.getPathTo().stream().map(Berth::getBerthName).collect(Collectors.toList()));
        ret.setSpeedLimit(b.getSpeedLimit());
        ret.setOccupiers(new ArrayList<>());
        return ret;
    }


    public boolean isFree(RunningRouteStop routeStop) {
        if(trackMap.containsKey(routeStop.getBerth().getBerthId())) {
            LiveBerth lBerth = trackMap.get(routeStop.getBerth().getBerthId());
            return !lBerth.getMultiOccupancy() || lBerth.getOccupiers().isEmpty();
        } else {
            log.error("Could not find Berth with ID : " + routeStop.getBerth().getBerthId() +
                    "( " + routeStop.getBerth().getBerthName() + ")");
            return false;
        }
    }
}
