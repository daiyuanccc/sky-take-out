package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    // 查询营业额(优化前,多次查询数据库)
    /*@Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //创建集合存放begin到end的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            //计算日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询date的营业额
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //select sum(amount) from orders where order_time > beginTime and order_time < endTime and status = 5
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumMap(map);
            //如果为空，则设置为0.0
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        // 使用 StringUtils.join() 方法将日期列表转换为字符串
        String join = StringUtils.join(dateList, ",");
        String join1 = StringUtils.join(turnoverList, ",");
        return TurnoverReportVO
                .builder()
                .dateList(join)
                .turnoverList(join1)
                .build();
    }*/


    /**
     * 批量查询营业额(优化后)
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 构建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();

        // 创建日期范围的SQL查询
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询时间段内的营业额，按日期分组
        Map<String, Object> params = new HashMap<>();
        params.put("begin", beginTime);
        params.put("end", endTime);
        params.put("status", Orders.COMPLETED);

        // 查询日期区间内每一天的营业额
        List<Map<String, Object>> result = orderMapper.sumTurnoverByDate(params);

        // 处理查询结果并构建日期和营业额列表
        Map<LocalDate, Double> turnoverMap = new HashMap<>();
        for (Map<String, Object> row : result) {
            LocalDate date = ((java.sql.Date) row.get("date")).toLocalDate();
            Double turnover = ((BigDecimal) row.get("turnover")).doubleValue();
            turnoverMap.put(date, turnover == null ? 0.0 : turnover);
        }

        // 填充完整的日期列表并获取对应的营业额，如果查询结果中没有则补0
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            turnoverList.add(turnoverMap.getOrDefault(begin, 0.0));
            begin = begin.plusDays(1);
        }

        // 使用 StringUtils.join() 方法将日期列表和营业额列表转换为字符串
        String dateStr = StringUtils.join(dateList, ",");
        String turnoverStr = StringUtils.join(turnoverList, ",");

        return TurnoverReportVO
                .builder()
                .dateList(dateStr)
                .turnoverList(turnoverStr)
                .build();
    }


    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 创建集合存放begin到end的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            // 计算日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 查询用户新增量
        List<Integer> newUserList = new ArrayList<>();
        // 查询用户总数
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 查询date的新增用户数量
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            // 总用户数量
            Integer totalUser = userMapper.countByMap(map);

            map.put("begin", beginTime);
            // 新增用户数量
            Integer newUser = userMapper.countByMap(map);

            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }

        log.info("总用户数量:{}", totalUserList);
        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }



}
