package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
    @Autowired
    private WorkspaceService workspaceService;

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

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 创建集合存放begin到end的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            // 计算日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 查询订单总数
        List<Integer> orderCountList = new ArrayList<>();
        // 查询有效订单总数
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // 查询订单总数
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            // 有效订单数量
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        // 计算时间区间内的总订单数量
        Integer totalOrderCount = orderCountList.stream().mapToInt(Integer::intValue).sum();
        // 计算时间区间内的有效订单数量
        Integer validOrderCount = validOrderCountList.stream().mapToInt(Integer::intValue).sum();
        // 计算订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount.doubleValue();
        }
        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销售Top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        // 查询销量top10
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        if (salesTop10 != null) {
            // 获取Top10商品名字
            List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
            // 获取Top10商品销量
            List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
            // 将数据封装到VO中并返回
            return SalesTop10ReportVO
                    .builder()
                    .nameList(StringUtils.join(nameList, ","))
                    .numberList(StringUtils.join(numberList, ","))
                    .build();
        }
        return null;
    }


    /**
     * 查询订单数量
     *
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }


    /**
     * 导出Excel
     *
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 查询数据库获取营业数据(30天)
        LocalDate beginTime = LocalDate.now().minusDays(30);
        LocalDate endTime = LocalDate.now().plusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(beginTime, LocalTime.MIN), LocalDateTime.of(endTime, LocalTime.MAX));
        // 通过POI 导出Excel
        // 基于模板创建Excel
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            // 获取第一个sheet
            XSSFSheet sheet = excel.getSheet("Sheet1");
            // 填充数据(日期...)
            sheet.getRow(1).getCell(1).setCellValue(beginTime + "至" + endTime);
            // 填充数据(营业额...)
            XSSFRow row = sheet.getRow(3);// 获取第4行
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            row = sheet.getRow(4);// 获取第4行
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            // 填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = beginTime.plusDays(i);
                // 获取一天的数据
                BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date.plusDays(1), LocalTime.MIN));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(data.getTurnover());
                row.getCell(3).setCellValue(data.getValidOrderCount());
                row.getCell(4).setCellValue(data.getOrderCompletionRate());
                row.getCell(5).setCellValue(data.getUnitPrice());
                row.getCell(6).setCellValue(data.getNewUsers());
            }

            // 输出输出流下载到浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            // 关闭资源
            in.close();
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
