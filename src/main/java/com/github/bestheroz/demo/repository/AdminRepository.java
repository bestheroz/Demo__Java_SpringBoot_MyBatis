package com.github.bestheroz.demo.repository;

import com.github.bestheroz.demo.entity.Admin;
import com.github.bestheroz.standard.common.mybatis.SqlRepository;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AdminRepository extends SqlRepository<Admin> {}
