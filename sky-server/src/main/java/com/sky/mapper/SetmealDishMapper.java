package com.sky.mapper;


import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
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

    /**
     * 保存套餐菜品信息
     * @param setmealDishes
     */
    void insertDishsBySetmealId(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id查询套餐菜品
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{dishId}")
    List<SetmealDish> getByDishId(Long id);

    /**
     * 根据套餐id删除套餐菜品
     * @param id
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{id}")
    void deleteByDishId(Long id);

    /**
     * 根据菜品id批量删除套餐菜品
     * @param ids
     */
    void deleteByDishIds(List<Long> ids);
}
