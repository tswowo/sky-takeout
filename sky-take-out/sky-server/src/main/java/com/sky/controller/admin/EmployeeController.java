package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.QueryAnnotation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController("adminEmployeeController")
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "管理端员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();
        log.info("生成jwt令牌：{}", token);
        return Result.success(employeeLoginVO);
    }

    /**
     * 退出登录
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出")
    public Result<String> logout() {
        log.info("员工退出");
        return Result.success();
    }

    /**
    * 员工分页查询
    *
     * @param employeePageQueryDTO 员工分页查询参数
     * @return Result<PageResult> 分页查询结果
    */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> pageEmployee(@ModelAttribute EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询：{}", employeePageQueryDTO);
        return employeeService.pageEmployee(employeePageQueryDTO);
    }

    /**
     * 新增员工
     * @param employeeDTO
     * @return Result<String>
     */
    @PostMapping
    @ApiOperation("新增员工")
    public Result<String> createEmployee(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工：{}", employeeDTO);
        return employeeService.createEmployee(employeeDTO);
    }

    /**
     * id查询员工
     * @param id
     * @return employee
     */
    @GetMapping("/{id}")
    @ApiOperation("id查询员工")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("id查询员工：{}", id);
        return employeeService.getById(id);
    }

    /**
     * 修改密码
     * @param passwordEditDTO
     * @return Result<String>
     */
    @PutMapping("/editPassword")
    @ApiOperation("修改员工密码")
    public Result<String> editPassword(@RequestBody PasswordEditDTO passwordEditDTO){
        log.info("修改员工密码：{}", passwordEditDTO);
        return employeeService.editPassword(passwordEditDTO);
    }

    /**
     * 更改员工状态
     * @param status
     * @return Result<String>
     */
    @PostMapping("/status/{status}")
    @ApiOperation("更改员工状态")
    public Result<String> setEmployeeStatus(@PathVariable Integer status,@RequestParam(required = true) Long id){
        log.info("更改员工状态:{},status{}", status,id);
        return employeeService.setEmployeeStatus(status,id);
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     * @return Result<String>
     */
    @PutMapping
    @ApiOperation("修改员工信息")
    public Result<String> updateEmployee(@RequestBody EmployeeDTO employeeDTO){
        log.info("修改员工信息：{}", employeeDTO);
        return employeeService.updateEmployee(employeeDTO);
    }
}
