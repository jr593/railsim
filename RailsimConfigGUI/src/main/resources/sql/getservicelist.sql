select ts.id as service_id, ts.origin , ts.destination , ts.start_time, rs.berth_id , b.berth_name ,
       rs.arrival_time ,rs.departure_time ,rs.route_stop_number
from train_service ts 
 join route_stop rs on ts.id = rs.service_id
 join berths b on rs.berth_id = b.berth_id 
 --where b.direction =1
 where ts.id = 3469
order by ts.start_time , rs.route_stop_number 