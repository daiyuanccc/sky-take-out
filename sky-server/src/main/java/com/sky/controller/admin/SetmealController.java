package com.sky.controller.admin;

import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;


    /**
     * 新增套餐
     *
     * @param setmealDTO
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     *
     * @param
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("菜品分页查询{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id查询套餐和菜品信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐信息:{}", id);
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐信息:{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 修改套餐起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<String> setStatus(@PathVariable Integer status, Long id) {
        log.info("启用或禁用套餐:{}", id);
        setmealService.setStatus(status, id);
        return Result.success();
    }


    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> deleteBatch(@RequestParam List<Long> ids) {
        log.info("批量删除套餐:{}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }
}
