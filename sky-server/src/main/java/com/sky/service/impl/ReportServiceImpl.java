package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;


    @Override
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
    }


    // 批量查询营业额(优化后)
    /*public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 创建集合存放begin到end的日期
        List<LocalDate> dateList = new ArrayList<>();
        if (begin.isAfter(end)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }

        LocalDate currentDate = begin;
        while (!currentDate.isAfter(end)) {
            dateList.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        // 批量查询所有日期的营业额
        List<Map<String, Object>> turnoverResults = new ArrayList<>();
        try {
            LocalDateTime startDateTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endDateTime = LocalDateTime.of(end, LocalTime.MAX);
            Map<String, Object> params = new HashMap<>();
            params.put("start", startDateTime);
            params.put("end", endDateTime);
            params.put("status", Orders.COMPLETED);
            turnoverResults = orderMapper.sumMapByDateRange(params); // 假设有一个批量查询方法
        } catch (Exception e) {
            log.error("批量查询营业额时发生异常", e);
            throw new RuntimeException("批量查询营业额时发生异常", e);
        }

        // 构建日期对应的营业额列表
        List<Double> turnoverList = new ArrayList<>(Collections.nCopies(dateList.size(), 0.0));
        for (Map<String, Object> result : turnoverResults) {
            LocalDate date = (LocalDate) result.get("date");
            Double turnover = (Double) result.get("turnover");
            int index = dateList.indexOf(date);
            if (index != -1) {
                turnoverList.set(index, turnover == null ? 0.0 : turnover);
            }
        }

        // 使用 StringBuilder 拼接日期和营业额列表
        StringBuilder dateBuilder = new StringBuilder();
        StringBuilder turnoverBuilder = new StringBuilder();
        for (int i = 0; i < dateList.size(); i++) {
            dateBuilder.append(dateList.get(i).toString());
            turnoverBuilder.append(turnoverList.get(i));
            if (i < dateList.size() - 1) {
                dateBuilder.append(",");
                turnoverBuilder.append(",");
            }
        }

        return TurnoverReportVO
                .builder()
                .dateList(dateBuilder.toString())
                .turnoverList(turnoverBuilder.toString())
                .build();
    }*/

}
