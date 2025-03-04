package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.SetmealVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.events.Event;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final Logger log = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        // 创建shoppingCart对象
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //获取用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        // 判断当前菜品或套餐是否在购物车中
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        log.info("购物车数据：{}", list);
        //判断当前菜品或套餐是否在购物车中
        if (!list.isEmpty()) {
            //如果在，获取数据
            ShoppingCart cart = list.get(0);
            //数量加一
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.update(cart);
        } else {
            //不在，添加到购物车，数量默认为1
            // 判断当前菜品还是套餐
            Long dishId = shoppingCart.getDishId();
            if (dishId != null) {//菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {//套餐
                Long setmealId = shoppingCart.getSetmealId();
                SetmealVO setmealVO = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmealVO.getName());
                shoppingCart.setImage(setmealVO.getImage());
                shoppingCart.setAmount(setmealVO.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.add(shoppingCart);
        }
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        //获取用户id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart
                .builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    /**
     * 减少购物车菜品或套餐
     *
     * @param shoppingCartDTO
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //获取用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //查询用户的购物车
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (!list.isEmpty()) {
            //获取购物车数据
            ShoppingCart cart = list.get(0);

            if (cart.getNumber() == 1) {
                shoppingCartMapper.delete(cart.getId());
            } else {
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.update(cart);
            }
        }
    }
}
