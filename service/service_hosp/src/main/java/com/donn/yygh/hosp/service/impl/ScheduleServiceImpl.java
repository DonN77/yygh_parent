package com.donn.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.donn.yygh.common.exception.YyghException;
import com.donn.yygh.hosp.repository.DepartmentRepository;
import com.donn.yygh.hosp.repository.HospitalRepository;
import com.donn.yygh.hosp.repository.ScheduleRepository;
import com.donn.yygh.hosp.service.ScheduleService;
import com.donn.yygh.model.hosp.BookingRule;
import com.donn.yygh.model.hosp.Department;
import com.donn.yygh.model.hosp.Hospital;
import com.donn.yygh.model.hosp.Schedule;
import com.donn.yygh.vo.hosp.BookingScheduleRuleVo;
import com.donn.yygh.vo.hosp.ScheduleOrderVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/27 21:39
 **/
@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    //map 封装了对象的各个属性（k：v）（属性名：属性值）
    public void saveSchedule(Map<String, Object> map) {
        //toJSONString 将传过来的map，转化为json字符串
        //parseObject 将json字符串，封装成一个对象
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(map), Schedule.class);
        Schedule platformScheduleId = scheduleRepository.findByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());
        if (platformScheduleId == null){   //无则新增
            //新增不需要指定 id，_id会自动生成
            //createTime、updateTime、isDeleted字段 需要初始化
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }else {   //有则修改
            schedule.setCreateTime(platformScheduleId.getCreateTime());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(platformScheduleId.getIsDeleted());
            //存在的话，需要设置 _id的值为原来的值，不然会重复插入
            schedule.setId(platformScheduleId.getId());
            scheduleRepository.save(schedule);
        }
    }

    @Override
    public Page<Schedule> getSchedulePage(Map<String, Object> map) {
        String hoscode = (String) map.get("hoscode");
        int limit = Integer.parseInt((String) map.get("limit"));
        int page = Integer.parseInt((String) map.get("page"));
        Schedule schedule = new Schedule();
        schedule.setHoscode(hoscode);
        Example<Schedule> example = Example.of(schedule);
        Pageable pageable = PageRequest.of(page-1,limit,Sort.by("createTime").ascending());
        Page<Schedule> all = scheduleRepository.findAll(example, pageable);
        return all;
    }

    @Override
    public void removeSchedule(Map<String, Object> map) {
        String hoscode = (String) map.get("hoscode");
        String hosScheduleId = (String) map.get("hosScheduleId");
        Schedule schedule = scheduleRepository.findByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        scheduleRepository.deleteById(schedule.getId());
    }

    @Override
    public Map<String, Object> page(Integer pageNum, Integer pageSize, String hoscode, String depcode) {
        //封装查询条件
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //mongoRepository适合简单查询，因为可以自定义
        //聚合查询：最好使用mongoTemple（聚合查询就是mysql的分组查询）
        // .first()，表示查分组字段，如果不调用这个方法，则表示不查询分组字段, .as()表示起别名
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.ASC,"workDate"),   //排序
                Aggregation.skip((pageNum - 1) * pageSize),   //从哪条开始分页
                Aggregation.limit(pageSize)   //一页多少条
        );  //表示聚合条件
        /*
           第一个参数Aggregation：表示聚合条件
           第二个参数InputType：表示输入类型，可以根据当前指定的字节码找到mongo对应集合
           第三个参数OutputType：表示输出类型，封装聚合后的信息
         */
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        //当前页对应的数据
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();
        //获取日期对应星期
        for (BookingScheduleRuleVo mappedResult : mappedResults) {
            String dayOfWeek = this.getDayOfWeek(new DateTime(mappedResult.getWorkDate()));
            mappedResult.setDayOfWeek(dayOfWeek);
        }

        //获取符合条件的总记录数
        Aggregation aggregation2 = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );  //表示聚合条件
        AggregationResults<BookingScheduleRuleVo> aggregate2 = mongoTemplate.aggregate(aggregation2, Schedule.class, BookingScheduleRuleVo.class);
        int total = aggregate2.getMappedResults().size();

        Map<String,Object> map = new HashMap<>();

        map.put("list",mappedResults);
        map.put("total",total);

        //通过hoscode查找医院，获取医院名字
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        Map<String,String> baseMap = new HashMap<>();
        //获取医院名字
        baseMap.put("hosname",hospital.getHosname());
        map.put("baseMap",baseMap);

        return map;
    }

    @Override
    public List<Schedule> detail(String hoscode, String depcode, String workDate) {
        //由于mongoDB是区分类型的，workDate在mongoDB中是以date类型存储的，所以需要将 String类型转换成date 类型
        Date date = new DateTime(workDate).toDate();
        List<Schedule> list = scheduleRepository.findByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,date);
        for (Schedule schedule : list) {
            this.packageSchedule(schedule);
        }
        //可以在前端控制，当天预约的时间超过 workDate时间，则显示为灰色（不可预约）
        return list;
    }

    @Override
    public Map<String, Object> getSchedulePageByCondition(String hoscode, String depcode, Integer pageNum, Integer pageSize) {
        //通过hospital的hoscode，获取医院的book规则
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        if (hospital == null){
            throw new YyghException(20001,"医院不存在");
        }
        BookingRule bookingRule = hospital.getBookingRule();

        //获取可预约日期分页数据,封装了当前页对应的时间列表
        IPage<Date> page = this.getListDate(pageNum,pageSize,bookingRule);
        List<Date> records = page.getRecords();

        //在Aggregation里不用再做分页，分页的逻辑在 封装的时间列表里了
        //records里的数据已经是做好分页操作的了
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(records);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.ASC,"workDate")
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        //将聚合后的List，根据workDate为键，对象本身为值，建立一个Map
        Map<Date, BookingScheduleRuleVo> map = aggregate.getMappedResults().stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));

        /**
         * 因为存在mongoDB里的数据，未必在可预约日期 当天有医生值班，
         * 所以需要从 当前页的第一天开始遍历，到最后一天进行判断，是否有人值班，对返回给前端的数据（list）进行设置
         * 如果当天没有医生值班，也需封装信息返回；利于前端进行页面渲染
         * 对于第一页的第一条数据要进行特殊处理
         * 对于最后一页的最后一条数据也要进行特殊处理
         */
        int size = records.size();
        List<BookingScheduleRuleVo> list = new ArrayList<>(size);
        //从当前页面的第一天进行遍历，到当前页面的最后一天，进行判断，设置值
        //然后将当前页面的每一天放到list中，返回给前端
        for (int i = 0; i < size; i++){
            Date date = records.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = map.get(date);

            //该天没有医生值班
            if (bookingScheduleRuleVo == null){
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                bookingScheduleRuleVo.setDocCount(0);
                bookingScheduleRuleVo.setAvailableNumber(-1);  //前端约定 -1表示无号
                bookingScheduleRuleVo.setReservedNumber(0);
            }

            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            bookingScheduleRuleVo.setDayOfWeek(this.getDayOfWeek(new DateTime(date)));
            bookingScheduleRuleVo.setStatus(0);  //正常

            //只有第一页的第一条（就是当天）存在 已停止挂号的情况
            if (i == 0 && pageNum == 1){
                DateTime dateTime = getDateTime(new Date(), bookingRule.getStopTime());
                //如果医院规定的当前的挂号截止时间在此时此刻之前，说明：此时此刻已经过了当天的挂号截止时间
                if (dateTime.isBeforeNow()){
                    bookingScheduleRuleVo.setStatus(-1); //已停止挂号
                }
            }
            //只有最后一页的最后一条 存在即将放好情况
            if (i == (size-1) && pageNum == page.getPages()){
                bookingScheduleRuleVo.setStatus(1);  //即将放号
            }

            list.add(bookingScheduleRuleVo);
        }

        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalRepository.findByHoscode(hoscode).getHosname());
        //科室
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode,depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());


        Map<String,Object> result = new HashMap<>();
        result.put("list",list);
        result.put("total",page.getTotal());
        result.put("baseMap",baseMap);

        return result;
    }

    @Override
    public Schedule detailByScheduleId(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        String depname = departmentRepository.findByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode()).getDepname();
        String hosname = hospitalRepository.findByHoscode(schedule.getHoscode()).getHosname();
        schedule.getParam().put("hosname",hosname);
        schedule.getParam().put("depname",depname);
        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleById(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        ScheduleOrderVo vo = new ScheduleOrderVo();
        //封装vo的所需信息
        BeanUtils.copyProperties(schedule,vo);

        Hospital hospital = hospitalRepository.findByHoscode(schedule.getHoscode());
        String depname = departmentRepository.findByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode()).getDepname();
        vo.setHosname(hospital.getHosname());
        vo.setDepname(depname);
        vo.setReserveDate(schedule.getWorkDate());
        vo.setReserveTime(schedule.getWorkTime());

        DateTime dateTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(hospital.getBookingRule().getQuitDay()).toDate(), hospital.getBookingRule().getQuitTime());
        vo.setQuitTime(dateTime.toDate());  //预约的退号截止时间
        //当天挂号的截止时间
        vo.setStopTime(this.getDateTime(schedule.getWorkDate(),hospital.getBookingRule().getStopTime()).toDate());

        return vo;
    }

    @Override
    public boolean updateAvailableNumber(String scheduleId, Integer availableNumber) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        schedule.setAvailableNumber(availableNumber);
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
        return true;
    }

    @Override
    public void cancelSchedule(String scheduleId) {
        Schedule schedule = scheduleRepository.findByHosScheduleId(scheduleId);
        schedule.setAvailableNumber(schedule.getAvailableNumber() + 1);
        scheduleRepository.save(schedule);

    }

    //封装 该页的 时间列表
    private IPage getListDate(Integer pageNum, Integer pageSize, BookingRule bookingRule) {
        Integer cycle = bookingRule.getCycle();
        //此时此刻是否已经超过了医院规定的当天挂号起始时间，如果此时此刻已经超过了：cycle+1
        //dateTime封装了今天医院规定的起始时间，2022-10-08 8:30
        DateTime dateTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        if (dateTime.isBeforeNow()){
            cycle++;
        }

        //封装符合条件的所有list，医院规定的，从今天起预约日期内的所有预约日期时间（10天或11天）
        List<Date> list = new ArrayList<>();
        for (int i = 0; i < cycle; i++){
            list.add(new DateTime(dateTime.plusDays(i).toString("yyyy-MM-dd")).toDate());
        }

        //封装当前页的list数据
        List<Date> result = new ArrayList<>();
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize;
        //防止最后一页的end超出实际size
        end = Math.min(end, list.size());
        for (int i = start; i < end; i++){
            Date date = list.get(i);
            result.add(date);
        }

        //page对象里封装 pageNum，pageSize，总记录数
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize, list.size());
        //封装 该页数据
        page.setRecords(result);

        return page;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    //封装排班详情其他值 医院名称、科室名称、日期对应星期
    private void packageSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname",hospitalRepository.findByHoscode(schedule.getHoscode()).getHosname());
        //设置科室名称
        schedule.getParam().put("depname",
                departmentRepository.findByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode()).getDepname());
        //设置日期对应星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }

    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
}
