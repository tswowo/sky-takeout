package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.ClearCache;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.utils.AliOssUtil;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 分页查询
     *
     * @param setmealPageQueryDTO
     * @return Result<PageResult>
     */
    @Override
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        if (setmealPageQueryDTO.getPage() < 1)
            setmealPageQueryDTO.setPage(1);
        if (setmealPageQueryDTO.getPageSize() < 1)
            setmealPageQueryDTO.setPageSize(10);
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.page(setmealPageQueryDTO);
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        return Result.success(pageResult, "");
    }

    /**
     * 新增套餐
     *
     * @param setmealDTO
     * @return Result<String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "setmealCache", key="#setmealDTO.categoryId")
    public Result<String> save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(setmealDTO, setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealMapper.insert(setmeal);

        Long id = setmeal.getId();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(id);
            setmealDishMapper.insert(setmealDish);
        });
        return Result.success("", "");
    }

    /**
     * 删除套餐
     *
     * @param ids
     * @return Result<String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result<String> deleteSetmeal(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return Result.error("参数错误");
        //统计有效的可删除的套餐
        List<Long> validIds = new ArrayList<>();
        ids.forEach(id -> {
            SetmealVO setmealVO = setmealMapper.getById(id);
            if (setmealVO == null)
                throw new BaseException("套餐不存在");
            if (setmealVO.getStatus() == 1)
                throw new BaseException(MessageConstant.SETMEAL_ON_SALE);
            validIds.add(id);
        });
        //删除套餐关联的菜品
        setmealDishMapper.deleteBySetmealIds(validIds);

        //删除OSS上的图片
        List<String> ImageFile = setmealMapper.getImageFileByIds(validIds);
        ImageFile.forEach(fileName -> {
            if (fileName != null)
                aliOssUtil.deleteImage(fileName);
        });

        //删除套餐
        setmealMapper.deleteSetmeal(validIds);
        return Result.success("", "");
    }

    /**
     * 根据id查询套餐
     *
     * @param id
     * @return Result<SetmealVO>
     */
    @Override
    public Result<SetmealVO> getById(Long id) {
        SetmealVO setmealVO = setmealMapper.getById(id);
        return Result.success(setmealVO, "");
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     * @return Result<String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result<String> update(SetmealDTO setmealDTO) {
        SetmealVO setmealVO = setmealMapper.getById(setmealDTO.getId());
        if (setmealVO == null)
            throw new BaseException("套餐不存在");
        if (setmealVO.getStatus() == 1)
            throw new BaseException("套餐正在售卖中，不能修改");
        if (setmealVO.getImage() != null && !setmealVO.getImage().equals(setmealDTO.getImage())) {
            aliOssUtil.deleteImage(setmealVO.getImage());
            setmealVO.setImage(setmealDTO.getImage());
        }

        //删除旧的套餐关联菜品
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        //插入新的套餐关联菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
            setmealDishMapper.insert(setmealDish);
        });

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        return Result.success("", "");
    }

    /**
     * 修改套餐状态
     *
     * @param status
     * @param id
     * @return Result<String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result<String> updateStatus(Integer status, Long id) {
        SetmealVO setmealVO = setmealMapper.getById(id);
        if (setmealVO == null)
            throw new BaseException("套餐不存在");
        //修改为启售时 查询是否有未起售菜品
        if (status == 1) {
            Integer countDisabledDish = setmealDishMapper.getCountDisableDishByDishId(id);
            if (countDisabledDish > 0)//存在未启售菜品则不能启售
                throw new BaseException(MessageConstant.SETMEAL_ENABLE_FAILED);
        }
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealVO, setmeal);
        setmeal.setStatus(status);
        setmealMapper.updateStatus(setmeal);
        return Result.success("", "");
    }

    /**
     * 根据分类id查询套餐基本信息
     *
     * @param categoryId
     * @return
     */
    @Override
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId")
    public Result<List<SetmealVO>> listByCategoryId(Long categoryId) {
        List<SetmealVO> setmealVOList = setmealMapper.listByCategoryId(categoryId);

        return Result.success(setmealVOList);
    }

    /**
     * 根据套餐id查询包含的菜品
     *
     * @param id
     * @return Result<List<DishItemVO>>
     */
    @Override
    public Result<List<DishItemVO>> getDishBySetmealId(Long id) {
        if (id == null || id < 0)
            throw new BaseException("分类id参数错误");
        List<DishItemVO> dishItemVOList = setmealMapper.getDishBySetmealId(id);
        return Result.success(dishItemVOList);
    }


}
