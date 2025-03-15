package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     *
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);



    /**
     * 用于替换微信支付更新数据库状态的问题
     *
     * @param orderStatus
     * @param orderPaidStatus
     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);

    /**
     * 分页查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    //select o.* , od.* as historyOrders from orders o left join order_detail od on o.id = od.order_id;
    //@Select("select * from order_detail where ")
    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    Page<Orders> pageQuery2(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 订单统计
     * @return
     */
    OrderStatisticsVO getStatistics();

    /**
     * 根据状态查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     *  根据条件统计金额
     * @param map
     * @return
     */
    Double sumMap(Map map);

    /**
     * 批量查询营业额
     * @param params
     * @return
     */
    @MapKey("date")
    List<Map<String, Object>> sumTurnoverByDate(Map<String, Object> params);

    /**
     * 动态条件查询订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 统计商品销量top10
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);

}
