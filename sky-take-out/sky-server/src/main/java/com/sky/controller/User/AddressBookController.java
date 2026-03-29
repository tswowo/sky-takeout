package com.sky.controller.User;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userAddressBookController")
@RequestMapping("/user/addressBook")
@Api(tags = "C端-地址簿接口")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @ApiOperation("查询当前登录用户的所有地址信息")
    @GetMapping("/list")
    public Result<List<AddressBook>> getAddressBookList() {
        log.info("查询当前登录用户所有地址信息");
        return addressBookService.list();
    }

    @ApiOperation("查询默认地址")
    @GetMapping("/default")
    public Result<AddressBook> getDefaultAddressBook() {
        log.info("查询默认地址");
        return addressBookService.getDefaultAddressBook();
    }

    @ApiOperation("查询当前登录用户的所有地址信息")
    @GetMapping("/{id}")
    public Result<AddressBook> getAddressBookById(@PathVariable Long id) {
        log.info("查询当前登录用户所有地址信息");
        return addressBookService.getByAddressBookId(id);
    }

    @ApiOperation("新增地址")
    @PostMapping
    public Result addAddressBook(@RequestBody AddressBook addressBook) {
        log.info("新增地址：{}", addressBook);
        return addressBookService.addAddressBook(addressBook);
    }

    @ApiOperation("根据id修改地址")
    @PutMapping
    public Result updateAddressBook(@RequestBody AddressBook addressBook) {
        log.info("修改地址：{}", addressBook);
        return addressBookService.updateAddressBook(addressBook);
    }

    @ApiOperation("设置默认地址")
    @PutMapping("/default")
    public Result setDefault(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址：{}", addressBook);
        return addressBookService.setDefaultById(addressBook);
    }

    @ApiOperation("根据id删除地址")
    @DeleteMapping
    public Result deleteAddressBook(@RequestParam Long id) {
        log.info("删除地址：{}", id);
        return addressBookService.deleteAddressBook(id);
    }

}
