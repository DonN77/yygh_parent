package com.donn.yygh.hosp.service;

import com.donn.yygh.model.hosp.Schedule;
import com.donn.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/27 21:39
 **/
public interface ScheduleService {
    void saveSchedule(Map<String, Object> map);

    Page<Schedule> getSchedulePage(Map<String, Object> map);

    void removeSchedule(Map<String, Object> map);

    Map<String, Object> page(Integer pageNum, Integer pageSize, String hoscode, String depcode);

    List<Schedule> detail(String hoscode, String depcode, String workDate);

    Map<String, Object> getSchedulePageByCondition(String hoscode, String depcode, Integer pageNum, Integer pageSize);

    Schedule detailByScheduleId(String scheduleId);

    ScheduleOrderVo getScheduleById(String scheduleId);

    boolean updateAvailableNumber(String scheduleId, Integer availableNumber);

    void cancelSchedule(String scheduleId);
}
