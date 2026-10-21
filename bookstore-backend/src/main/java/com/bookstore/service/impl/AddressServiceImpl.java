package com.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookstore.entity.Address;
import com.bookstore.mapper.AddressMapper;
import com.bookstore.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    public AddressServiceImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public List<Address> listByUser(Long userId) {
        LambdaQueryWrapper<Address> qw = new LambdaQueryWrapper<>();
        qw.eq(Address::getUserId, userId).orderByDesc(Address::getIsDefault);
        return addressMapper.selectList(qw);
    }

    @Override
    public void add(Long userId, String addr) {
        Address a = new Address();
        a.setUserId(userId);
        a.setAddress(addr);
        a.setIsDefault(0);
        // 如果这是第一个地址，设为默认
        if (addressMapper.selectCount(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId)) == 0) {
            a.setIsDefault(1);
        }
        addressMapper.insert(a);
    }

    @Override
    public void update(Long id, Long userId, String addr) {
        Address a = addressMapper.selectById(id);
        if (a != null && a.getUserId().equals(userId)) {
            a.setAddress(addr);
            addressMapper.updateById(a);
        }
    }

    @Override
    public void delete(Long id, Long userId) {
        Address a = addressMapper.selectById(id);
        if (a != null && a.getUserId().equals(userId)) {
            addressMapper.deleteById(id);
        }
    }

    @Override
    public void setDefault(Long id, Long userId) {
        // 先取消所有默认
        LambdaQueryWrapper<Address> qw = new LambdaQueryWrapper<>();
        qw.eq(Address::getUserId, userId);
        for (Address a : addressMapper.selectList(qw)) {
            a.setIsDefault(0);
            addressMapper.updateById(a);
        }
        // 设置新的默认
        Address a = addressMapper.selectById(id);
        if (a != null && a.getUserId().equals(userId)) {
            a.setIsDefault(1);
            addressMapper.updateById(a);
        }
    }
}
