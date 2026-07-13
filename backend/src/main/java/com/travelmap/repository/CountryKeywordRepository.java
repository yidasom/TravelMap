package com.travelmap.repository;

import com.travelmap.entity.CountryKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryKeywordRepository extends JpaRepository<CountryKeyword, Long> {

    boolean existsByKeywordIgnoreCase(String keyword);

    List<CountryKeyword> findAllByOrderByCountryNameAscKeywordAsc();
}
