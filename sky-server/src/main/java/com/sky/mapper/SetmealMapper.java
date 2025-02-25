package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.anno.AutoFIll;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 新增套餐
     * @param setmeal
     */
    @Insert("insert into setmeal (id, category_id, name, price, status, description, image, create_time, update_time, create_user, update_user) " +
            "values (#{id}, #{categoryId}, #{name}, #{price}, #{status}, #{description}, #{image}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFIll(OperationType.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(Setmeal setmeal);

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据id查询套餐信息
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    SetmealVO getById(Long id);

    /**
     * 修改套餐
     * @param setmeal
     */
    @AutoFIll(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name , sd.copies,d.image,d.description from setmeal_dish sd left join dish d " +
            "on sd.dish_id=d.id " +
            "where sd.setmeal_id=#{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
}
