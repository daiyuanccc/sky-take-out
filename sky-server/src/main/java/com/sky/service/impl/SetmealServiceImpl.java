package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        //1.保存套餐基本信息到setmeal表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        log.info("插入后的套餐ID: {}", setmeal.getId());
        //2.保存套餐菜品到setmeal_dish表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //3.判断套餐菜品是否可售
        for (SetmealDish setmealDish : setmealDishes) {
            Long dishId = setmealDish.getDishId();
            Dish dish = dishMapper.getById(dishId);
            if (dish.getStatus() == 0) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        if (!setmealDishes.isEmpty()) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmeal.getId());
            }
            log.info("套餐菜品信息:{}", setmealDishes);
            setmealDishMapper.insertDishsBySetmealId(setmealDishes);
        }
    }

    /**
     * 分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id查询套餐和菜品信息
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        //查询套餐基本信息
        SetmealVO setmealVO = setmealMapper.getById(id);
        //查询套餐菜品信息
        List<SetmealDish> setmealDishes = setmealDishMapper.getByDishId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //1.更新套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        //2.先删除套餐菜品信息
        //获取套餐id
        Long id = setmealDTO.getId();
        //从setmeal_dish表中删除该套餐id的数据
        setmealDishMapper.deleteByDishId(id);

        //3.再添加套餐菜品信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //4.判断套餐菜品是否可售
        //TODO 修改前端判断套餐菜品是否可售
        for (SetmealDish setmealDish : setmealDishes) {
            Long dishId = setmealDish.getDishId();
            Dish dish = dishMapper.getById(dishId);
            if (dish.getStatus() == 0) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        if (!setmealDishes.isEmpty()) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(id);
            }
            setmealDishMapper.insertDishsBySetmealId(setmealDishes);
        }
    }

    /**
     * 修改套餐状态
     *
     * @param status
     * @param id
     */
    @Override
    public void setStatus(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder()
                .status(status)
                .id(id)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断套餐是否为空
        if (ids == null || ids.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_NOT_FOUND);
        }
        //判断套餐售卖状态
        for (Long id : ids) {
            SetmealVO setmealVo = setmealMapper.getById(id);
            if (setmealVo.getStatus() == 1) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //批量删除套餐
        setmealMapper.deleteBatch(ids);

        //删除套餐菜品
        setmealDishMapper.deleteByDishIds(ids);

    }
}
