package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 员工分页查询
     * @param name
     * @return Page<Employee>
     */
    Page<Employee> pageQuery(String name);

    /**
     * 新增员工
     * @param employee
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Employee employee);

    /**
     * 根据id查询员工
     * @param id
     * @return Employee
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);

    /**
     * 修改员工信息
     * @param employee
     */
    @AutoFill(value = OperationType.UPDATE)
    void updateById(Employee employee);
}
