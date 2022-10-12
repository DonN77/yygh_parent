package com.donn.yygh.hosp.repository;

import com.donn.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/27 21:39
 **/
public interface ScheduleRepository extends MongoRepository<Schedule,String> {
    Schedule findByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    List<Schedule> findByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date date);

    Schedule findByHosScheduleId(String scheduleId);
}
