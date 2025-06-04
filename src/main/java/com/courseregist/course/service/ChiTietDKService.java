package com.courseregist.course.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.courseregist.course.repository.ChiTietDKRepository;

import java.sql.SQLException;

@Service
// Xử lý nghiệp vụ khi người dùng đăng kí học phần
public class ChiTietDKService {

    private final ChiTietDKRepository repository;

    @Autowired
    public ChiTietDKService(ChiTietDKRepository repository) {
        this.repository = repository;
    }

    public int getSoSinhVienDangKy(String maLop) throws SQLException {
        return repository.siSoDangKyHP(maLop);
    }
}
