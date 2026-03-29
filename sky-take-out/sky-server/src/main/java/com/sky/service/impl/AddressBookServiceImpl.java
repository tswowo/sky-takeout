package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.exception.BaseException;
import com.sky.mapper.AddressBookMapper;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 查询当前登录用户的所有地址信息
     *
     * @return 当前UserId关联的全部地址列表
     */
    @Override
    public Result<List<AddressBook>> list() {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(userId);
        List<AddressBook> list = addressBookMapper.listByAddressBook(addressBook);
        return Result.success(list);
    }

    /**
     * 查询当前登录用户的默认地址信息
     *
     * @return 默认地址信息
     */
    @Override
    public Result<AddressBook> getDefaultAddressBook() {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(userId);
        addressBook.setIsDefault(1);
        List<AddressBook> list = addressBookMapper.listByAddressBook(addressBook);

        if (list == null || list.isEmpty()) {
            return Result.success(null);
        } else if (list.size() > 1) {
            throw new BaseException("当前用户默认地址数量异常");
        }

        return Result.success(list.get(0));
    }

    /**
     * 根据id查询地址信息
     *
     * @param id AddressBook的id
     * @return id对应的AddressBook
     */
    @Override
    public Result<AddressBook> getByAddressBookId(Long id) {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = new AddressBook();
        addressBook.setId(id);
        addressBook.setUserId(userId);

        List<AddressBook> list = addressBookMapper.listByAddressBook(addressBook);
        if (list == null || list.isEmpty()) {
            throw new BaseException("当前用户没有此地址");
        } else if (list.size() > 1) {
            throw new BaseException("当前用户地址数量异常");
        }
        return Result.success(list.get(0));
    }


    /**
     * 新增地址
     *
     * @param addressBook
     * @return
     */
    @Override
    public Result addAddressBook(AddressBook addressBook) {
        //设置用户id
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        //如果这是第一个地址，则设置为默认地址
        AddressBook defaultAddressBook = new AddressBook();
        defaultAddressBook.setUserId(userId);
        defaultAddressBook.setIsDefault(1);
        List<AddressBook> list = addressBookMapper.listByAddressBook(defaultAddressBook);
        if (list == null || list.isEmpty()) {
            addressBook.setIsDefault(1);
        } else {
            addressBook.setIsDefault(0);
        }
        addressBookMapper.insert(addressBook);
        return Result.success();
    }

    /**
     * 修改地址
     *
     * @param addressBook
     */
    @Override
    public Result updateAddressBook(AddressBook addressBook) {
        //该用户是否存在该地址
        Long userId = BaseContext.getCurrentId();
        AddressBook existAddressBook = new AddressBook();
        existAddressBook.setId(addressBook.getId());
        existAddressBook.setUserId(userId);
        List<AddressBook> list = addressBookMapper.listByAddressBook(existAddressBook);
        if (list == null || list.isEmpty()) {
            throw new BaseException("当前用户没有此地址");
        }
        //修改为新地址
        addressBookMapper.update(addressBook);

        return Result.success();
    }

    /**
     * 设置为默认地址
     *
     * @param addressBook 新默认地址
     */
    @Override
    @Transactional
    public Result setDefaultById(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        //校验是否存在这个地址
        List<AddressBook> list = addressBookMapper.listByAddressBook(addressBook);
        if (list == null || list.isEmpty()) {
            throw new BaseException("当前用户没有此地址");
        } else if (list.size() > 1) {
            throw new BaseException("当前用户地址数量异常");
        }
        //清除所有默认地址
        log.info("清除所有默认地址");
        AddressBook clearDefaultAddressBook = new AddressBook();
        clearDefaultAddressBook.setUserId(userId);
        clearDefaultAddressBook.setIsDefault(0);
        addressBookMapper.update(clearDefaultAddressBook);
        //设置这个地址为默认地址
        log.info("设置{}为默认地址", addressBook);
        AddressBook existAddressBook = list.get(0);
        existAddressBook.setIsDefault(1);
        addressBookMapper.update(existAddressBook);

        return Result.success();
    }

    /**
     * 根据id删除地址
     *
     * @param id 删除的AddressBook-id
     */
    @Override
    public Result deleteAddressBook(Long id) {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = new AddressBook();
        addressBook.setId(id);
        addressBook.setUserId(userId);
        List<AddressBook> list = addressBookMapper.listByAddressBook(addressBook);
        if (list == null || list.isEmpty()) {
            throw new BaseException("当前用户没有此地址");
        } else if (list.size() > 1) {
            throw new BaseException("当前用户地址数量异常");
        }
        addressBookMapper.delete(id);

        return Result.success();
    }
}
