package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.ClearCache;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.BaseException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.utils.AliOssUtil;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return Result<PageResult>
     */
    @Override
    public Result<PageResult> pageDish(DishPageQueryDTO dishPageQueryDTO) {
        if (dishPageQueryDTO.getPage() < 1)
            dishPageQueryDTO.setPage(1);
        if (dishPageQueryDTO.getPageSize() < 1)
            dishPageQueryDTO.setPageSize(10);

        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        return Result.success(pageResult);
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @Override
    public Result<List<DishVO>> listDish(Long categoryId) {
        if (categoryId < 0)
            return Result.error("分类id参数错误");
        return Result.success(dishMapper.listDish(categoryId), "");
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return Result<DishVO>
     */
    @Override
    public Result<DishVO> getDishById(Long id) {
        DishVO dishVO = dishMapper.getDishVOById(id);
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        dishVO.setFlavors(flavors);
        return Result.success(dishVO, "");
    }

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return Result<String></String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "dishCache", key = "#dishDTO.categoryId")
    public Result<String> saveDish(DishDTO dishDTO) {
        if (dishDTO.getCategoryId() == null)
            throw new BaseException("菜品分类id参数错误");
        if (dishDTO.getImage() == null || dishDTO.getImage().equals(""))
            throw new BaseException("菜品图片参数错误");
        if (dishDTO.getName() == null || dishDTO.getName().equals(""))
            throw new BaseException("菜品名称参数错误");
        if (dishDTO.getPrice() == null)
            throw new BaseException("菜品价格参数错误");

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.insertDish(dish);
        Long dishId = dish.getId();

        for (DishFlavor flavor : dishDTO.getFlavors()) {
            flavor.setDishId(dishId);
            dishFlavorMapper.insertDishFlavor(flavor);
        }
        return Result.success("", "");
    }


    /**
     * 删除菜品
     *
     * @param idList
     * @return Result<String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<String> deleteDishById(List<Long> idList) {
        // 先校验所有菜品
        for (Long id : idList) {
            Dish dish = dishMapper.getDishById(id);
            if (dish == null)
                throw new BaseException("菜品不存在");
            if (dish.getStatus() == 1)
                throw new BaseException(MessageConstant.DISH_ON_SALE);
            if (dishMapper.getCountDishByDishIdInSetmeal(id) > 0)
                throw new BaseException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 校验通过后统一删除
        for (Long id : idList) {
            Dish dish = dishMapper.getDishById(id);
            if (dish.getImage() != null && !dish.getImage().equals("")) {
                aliOssUtil.deleteImage(dish.getImage());
            }
            dishFlavorMapper.deleteByDishId(id);
            dishMapper.deleteDishById(id);
        }

        return Result.success("", "");
    }


    /**
     * 修改菜品起售、停售状态
     *
     * @param status
     * @param id
     * @return Result<String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<String> updateDishStatus(Integer status, Long id) {
        if (status != 0 && status != 1)
            throw new BaseException("菜品状态参数错误");

        Dish dish = dishMapper.getDishById(id);
        if (dish == null)
            throw new BaseException("菜品不存在");

        // 如果是禁用操作
        if (status == 0) {
            // 先禁用引用该菜品的套餐
            setmealDishMapper.disableSetmealByDishId(id);
        }
        //更新状态
        dish.setStatus(status);
        dishMapper.updateDishStatus(dish);

        return Result.success("", "");
    }


    /**
     * 修改菜品
     *
     * @param dishDTO
     * @return Result<String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<String> updateDish(DishDTO dishDTO) {
        //校验菜品
        Dish dish = dishMapper.getDishById(dishDTO.getId());
        if (dish == null)
            throw new BaseException("菜品不存在");
        //删除业务逻辑
        if (dish.getImage() != null && !dish.getImage().equals("") && !Objects.equals(dishDTO.getImage(), dish.getImage())) {
            aliOssUtil.deleteImage(dish.getImage());
        }
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.updateDish(dish);

        if (dishDTO.getFlavors() != null) {
            dishFlavorMapper.deleteByDishId(dishDTO.getId());
            for (DishFlavor flavor : dishDTO.getFlavors()) {
                flavor.setDishId(dish.getId());
                dishFlavorMapper.insertDishFlavor(flavor);
            }
        }


        return Result.success("", "");
    }

    /**
     * 根据分类id查询菜品详细信息列表
     *
     * @param categoryId
     * @return Result<List<DishVO>>
     */
    @Override
    @Cacheable(cacheNames = "dishCache", key = "#categoryId")
    public Result<List<DishVO>> listDishByCategoryId(Long categoryId) {
        if (categoryId == null || categoryId < 0)
            throw new BaseException("分类id参数错误");
        List<DishVO> dishVOList = dishMapper.listDishWithFlavors(categoryId);

        return Result.success(dishVOList);
    }
}
