package com.bookstore.service;

import com.bookstore.entity.Address;
import java.util.List;

public interface AddressService {
    List<Address> listByUser(Long userId);
    void add(Long userId, String address);
    void update(Long id, Long userId, String address);
    void delete(Long id, Long userId);
    void setDefault(Long id, Long userId);
}
