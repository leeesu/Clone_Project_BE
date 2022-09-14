package com.example.intermediate.repository;

import com.example.intermediate.domain.Img;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImgRepository extends JpaRepository<Img, Long> {
    List<Img> findByPost_Id(Long id);
    List<Img> deleteByPost_Id(Long id);
}
