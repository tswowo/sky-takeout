package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
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
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return Result<PageResult>
     */
    @Override
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        if(setmealPageQueryDTO.getPage()<1)
            setmealPageQueryDTO.setPage(1);
        if(setmealPageQueryDTO.getPageSize()<1)
            setmealPageQueryDTO.setPageSize(10);
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page=setmealMapper.page(setmealPageQueryDTO);
        PageResult pageResult=new PageResult(page.getTotal(),page.getResult());
        return Result.success(pageResult,"");
    }

    /**
     * 新增套餐
     * @param setmealDTO
     * @return Result<String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> save(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();

        BeanUtils.copyProperties(setmealDTO,setmeal);
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        setmealMapper.insert(setmeal);

        Long id=setmeal.getId();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(id);
            setmealDishMapper.insert(setmealDish);
        });
        return Result.success("", "");
    }

    /**
     * 删除套餐
     * @param ids
     * @return Result<String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteSetmeal(List<Long> ids) {
        if(ids==null||ids.size()==0)
            return Result.error("参数错误");
        setmealDishMapper.deleteBySetmealIds(ids);

        List<String> ImageFile=setmealMapper.getImageFileByIds(ids);
        ImageFile.forEach(fileName -> {
            if(fileName!=null)
                aliOssUtil.deleteImage(fileName);
        });
        setmealMapper.deleteSetmeal(ids);
        return Result.success("", "");
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return Result<SetmealVO>
     */
    @Override
    public Result<SetmealVO> getById(Long id) {
        SetmealVO setmealVO=setmealMapper.getById(id);
        return Result.success(setmealVO,"");
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return Result<String>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> update(SetmealDTO setmealDTO) {
        SetmealVO setmealVO=setmealMapper.getById(setmealDTO.getId());
        if(setmealVO==null)
            throw new BaseException("套餐不存在");
        if(setmealVO.getStatus()==1)
            throw new BaseException("套餐正在售卖中，不能修改");
        if(setmealVO.getImage()!=null&&!setmealVO.getImage().equals(setmealDTO.getImage())){
            aliOssUtil.deleteImage(setmealVO.getImage());
            setmealVO.setImage(setmealDTO.getImage());
        }

        //删除旧的套餐关联菜品
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        //插入新的套餐关联菜品
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
            setmealDishMapper.insert(setmealDish);
        });

        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);

        return Result.success("", "");
    }

    @Override
    public Result<String> updateStatus(Integer status, Long id) {
        SetmealVO setmealVO=setmealMapper.getById(id);
        if(setmealVO==null)
            throw new BaseException("套餐不存在");
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealVO,setmeal);
        setmeal.setStatus(status);
        setmealMapper.updateStatus(setmeal);
        return Result.success("", "");
    }
}
