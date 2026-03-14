package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.result.Result;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     * @return Result<PageResult>
     */
    Result<PageResult> pageEmployee(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 新增员工
     * @param employeeDTO
     * @return Result<String>
     */
    Result<String> createEmployee(EmployeeDTO employeeDTO);

    /**
     * 根据id查询员工
     * @param id
     * @return Result<Employee>
     */
    Result<Employee> getById(Long id);

    /**
     * 修改员工密码
     * @param passwordEditDTO
     * @return
     */
    Result<String> editPassword(PasswordEditDTO passwordEditDTO);

    /**
     * 修改员工状态
     * @param status
     * @param id
     * @return
     */
    Result<String> setEmployeeStatus(Integer status, Long id);

    /**
     * 修改员工信息
     * @param employeeDTO
     * @return Result<String>
     */
    Result<String> updateEmployee(EmployeeDTO employeeDTO);
}
