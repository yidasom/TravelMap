package com.travelmap.repository;

import com.travelmap.entity.CityKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityKeywordRepository extends JpaRepository<CityKeyword, Long> {

    boolean existsByKeywordIgnoreCase(String keyword);

    List<CityKeyword> findAllByOrderByCityNameAscKeywordAsc();
}
