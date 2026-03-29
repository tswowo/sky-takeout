package com.sky.service;

import com.sky.entity.AddressBook;
import com.sky.result.Result;

import java.util.List;

public interface AddressBookService {
    Result<List<AddressBook>> list();

    Result<AddressBook> getDefaultAddressBook();

    Result<AddressBook> getByAddressBookId(Long id);

    Result addAddressBook(AddressBook addressBook);

    Result updateAddressBook(AddressBook addressBook);

    Result setDefaultById(AddressBook addressBook);

    Result deleteAddressBook(Long id);
}
