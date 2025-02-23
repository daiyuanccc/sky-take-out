package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@Slf4j
@RequestMapping("/admin/dish")
@RestController
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品{}", ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改菜品售卖状态
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result setStatus(@PathVariable Integer status, Long id) {
        log.info("修改菜品id:{}状态为:{}", id, status);
        dishService.setStatus(status, id);
        return Result.success();
    }


    /**
     * 根据id查询菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id:{}查询菜品信息", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        log.info("菜品信息:{}", dishVO);
        return Result.success(dishVO);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<Dish>> getByCategoryId(@RequestParam Long categoryId) {
        log.info("根据分类id查询菜品:{}", categoryId);
        List<Dish> dish = dishService.getByCategoryId(categoryId);

        return Result.success(dish);
    }
    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息:{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }


}
