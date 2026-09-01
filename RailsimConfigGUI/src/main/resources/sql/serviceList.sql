SELECT  ts.start_time , ts.origin, ts.destination ,ts.service_class ,  r.stop_type , 
        b.berth_name , r.stop_type , r.arrival_time , r.departure_time 
FROM public.route_stop r, public.berths b , train_service ts 
where r.berth_id = b.berth_id and r.service_id = ts.id 
order by ts.start_time 