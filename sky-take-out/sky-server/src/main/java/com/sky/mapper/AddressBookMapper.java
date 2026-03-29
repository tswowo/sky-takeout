package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    /**
     * 根据AddressBook查询
     *
     * @param addressBook
     * @return 符合条件的地址列表
     */
    List<AddressBook> listByAddressBook(AddressBook addressBook);

    /**
     * 新增地址
     *
     * @param addressBook
     */
    void insert(AddressBook addressBook);

    /**
     * 修改地址
     *
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 根据地址id删除地址
     *
     * @param id 要删除的地址的主键id
     */
    @Delete("delete from address_book where id = #{id}")
    void delete(Long id);
}
