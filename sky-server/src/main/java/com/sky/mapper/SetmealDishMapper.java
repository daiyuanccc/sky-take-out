package com.sky.mapper;


import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询套餐id(一个菜品可能有多个套餐，所以返回List集合)
     * @param dishIds
     * @return
     */
    //select setmeal_id from setmeal_dish where dish_id in (1,2,3,4,5)
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);
}
