package com.courseregist.course.service;

import com.courseregist.course.entity.HocKy;
import com.courseregist.course.repository.HocKyRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
// Xử lý nghiệp vụ khi lấy danh sách học kì
public class HocKyService {

    private final HocKyRepository hockyRepository;

    public HocKyService(HocKyRepository repository) {
        this.hockyRepository = repository;
    }

    public List<HocKy> getAllHocKy() {
        return hockyRepository.getAllHocKy();
    }

    public List<String> getDistinctNamHoc() {
        return hockyRepository.getDistinctNamHoc();
    }

    // for chart
    public List<String> findDistinctHocKy() {
        return hockyRepository.findDistinctHocKy();
    }

}
