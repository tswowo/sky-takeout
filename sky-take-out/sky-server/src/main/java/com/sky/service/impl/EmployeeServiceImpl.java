package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.BaseException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.LOGIN_FAILED+MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        //进行md5加密，然后再进行比对
        if (!md5Password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.LOGIN_FAILED+MessageConstant.PASSWORD_ERROR);
        }

        if (Objects.equals(employee.getStatus(), StatusConstant.DISABLE)) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.LOGIN_FAILED+MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     * @return Result<PageResult>
     */
    @Override
    public Result<PageResult> pageEmployee(EmployeePageQueryDTO employeePageQueryDTO){
        if(employeePageQueryDTO.getPage()<1)
            employeePageQueryDTO.setPage(1);
        if(employeePageQueryDTO.getPageSize()<0||employeePageQueryDTO.getPageSize()>100)
            employeePageQueryDTO.setPageSize(10);

        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO.getName());

        long total = page.getTotal();
        PageResult pageResult = new PageResult(total,page.getResult());
        return Result.success(pageResult);
    }

    /**
     * 新增员工
     * @param employeeDTO
     * @return Result<String>
     */
    @Override
    public Result<String> createEmployee(EmployeeDTO employeeDTO) {
        //检索必须的参数，没有的话抛出异常
        if(employeeDTO.getUsername()==null||employeeDTO.getUsername().equals("")
        || employeeDTO.getName()==null||employeeDTO.getName().equals("")
        || employeeDTO.getPhone()==null||employeeDTO.getPhone().equals("")
        || employeeDTO.getSex()==null||employeeDTO.getSex().equals("")
        || employeeDTO.getIdNumber()==null||employeeDTO.getIdNumber().equals("")){
            throw new BaseException("必须的参数不能为空");
        }

        if(employeeMapper.getByUsername(employeeDTO.getUsername())!=null){
            throw new BaseException("用户名已存在");
        }
        if (employeeDTO.getPhone().length() != 11 || !employeeDTO.getPhone().matches("\\d+")) {
            throw new BaseException("手机号格式不正确");
        }
        if (employeeDTO.getIdNumber().length() != 15 && employeeDTO.getIdNumber().length() != 18) {
            throw new BaseException("身份证号格式不正确");
        }
        if (employeeDTO.getUsername().isEmpty() || employeeDTO.getUsername().length() > 32) {
            throw new BaseException("用户名长度必须在32位之内");
        }

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes(StandardCharsets.UTF_8)));
        employee.setStatus(StatusConstant.DISABLE);
        try {
            employeeMapper.insert(employee);
        } catch (Exception e) {
            throw new BaseException("添加员工失败");
        }
        return Result.success("");
    }

    /**
     * 根据id查询员工
     * @param id
     * @return Result<Employee>
     */
    @Override
    public Result<Employee> getById(Long id) {
        if(id==null)
            throw new BaseException("id不能为空");
        Employee employee = employeeMapper.getById(id);
        if(employee==null)
            throw new BaseException("员工不存在");
        return Result.success(employee,"");
    }

    /**
     * 修改员工密码
     * @param passwordEditDTO
     * @return Result<String>
     */
    @Override
    public Result<String> editPassword(PasswordEditDTO passwordEditDTO){
        Employee employee = employeeMapper.getById(BaseContext.getCurrentId());
        if(employee==null)
            throw new BaseException(MessageConstant.PASSWORD_EDIT_FAILED+"员工不存在");
        if(!employee.getPassword().equals(DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes(StandardCharsets.UTF_8))))
            throw new BaseException(MessageConstant.PASSWORD_EDIT_FAILED+"旧密码错误");
        if(passwordEditDTO.getNewPassword().length()<6)
            throw new BaseException(MessageConstant.PASSWORD_EDIT_FAILED+"新密码不能小于6位");

        employee.setId(BaseContext.getCurrentId());
        employee.setPassword(DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes(StandardCharsets.UTF_8)));
        employeeMapper.updateById(employee);
        return Result.success("");
    }

    /**
     * 修改员工状态
     * @param status
     * @param id
     * @return Result<String>
     */
    @Override
    public Result<String> setEmployeeStatus(Integer status, Long id) {
        if(status==null)
            throw new BaseException("状态不能为空");
        if(id==null)
            throw new BaseException("id不能为空");
        if(id==1)
            throw new BaseException("超级管理员不能修改");
        if(id.equals(BaseContext.getCurrentId()))
            throw new BaseException("不能修改自己的状态");

        Employee employee = employeeMapper.getById(id);
        if(employee==null)
            throw new BaseException("员工不存在");
        employee.setStatus(status);
        employeeMapper.updateById(employee);
        return Result.success("","");
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     * @return Result<String>
     */
    @Override
    public Result<String> updateEmployee(EmployeeDTO employeeDTO) {
        if(employeeDTO.getId()==null)
            throw new BaseException("id不能为空");

        Employee employee=employeeMapper.getById(employeeDTO.getId());
        if(employee==null)
            throw new BaseException("员工不存在");

        Employee existEmployee = employeeMapper.getByUsername(employeeDTO.getUsername());
        if(existEmployee != null && !existEmployee.getId().equals(employeeDTO.getId())){
            throw new BaseException("用户名已存在");
        }
        if (employeeDTO.getPhone().length() != 11 || !employeeDTO.getPhone().matches("\\d+")) {
            throw new BaseException("手机号格式不正确");
        }
        if (employeeDTO.getIdNumber().length() != 15 && employeeDTO.getIdNumber().length() != 18) {
            throw new BaseException("身份证号格式不正确");
        }
        if (employeeDTO.getUsername().isEmpty() || employeeDTO.getUsername().length() > 32) {
            throw new BaseException("用户名长度必须在32位之内");
        }

        BeanUtils.copyProperties(employeeDTO,employee);

        employeeMapper.updateById(employee);
        return Result.success("");
    }
}
