package com.travelmap.repository;

import com.travelmap.entity.VisitCountry;
import com.travelmap.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitCountryRepository extends JpaRepository<VisitCountry, Long> {
    
    List<VisitCountry> findByVideo(Video video);
    
    List<VisitCountry> findByVideoId(Long videoId);
    
    List<VisitCountry> findByCountryCode(String countryCode);
    
    List<VisitCountry> findByCountryName(String countryName);
    
    List<VisitCountry> findByContinent(String continent);
    
    List<VisitCountry> findByDetectionMethod(String detectionMethod);
    
    @Query("SELECT DISTINCT vc.countryCode FROM VisitCountry vc")
    List<String> findDistinctCountryCodes();
    
    @Query("SELECT DISTINCT vc.countryName FROM VisitCountry vc")
    List<String> findDistinctCountryNames();
    
    @Query("SELECT DISTINCT vc.continent FROM VisitCountry vc WHERE vc.continent IS NOT NULL")
    List<String> findDistinctContinents();
    
    @Query("SELECT vc.countryCode, COUNT(vc) as visitCount FROM VisitCountry vc GROUP BY vc.countryCode ORDER BY visitCount DESC")
    List<Object[]> findCountryVisitCounts();
    
    @Query("SELECT vc.countryCode, COUNT(DISTINCT vc.video.user) as youtuberCount FROM VisitCountry vc GROUP BY vc.countryCode ORDER BY youtuberCount DESC")
    List<Object[]> findCountryYoutuberCounts();
    
    @Query("SELECT vc FROM VisitCountry vc JOIN vc.video v JOIN v.user u " +
           "WHERE (:userId IS NULL OR u.id = :userId) " +
           "AND (:countryCode IS NULL OR vc.countryCode = :countryCode) " +
           "AND (:continent IS NULL OR vc.continent = :continent) " +
           "AND (:year IS NULL OR YEAR(v.uploadDate) = :year) " +
           "AND (COALESCE(:startDate, v.uploadDate) <= v.uploadDate) " +
           "AND (COALESCE(:endDate, v.uploadDate) >= v.uploadDate)")
    List<VisitCountry> findByFilters(@Param("userId") Long userId,
                                   @Param("countryCode") String countryCode,
                                   @Param("continent") String continent,
                                   @Param("year") Integer year,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(vc) FROM VisitCountry vc WHERE vc.video = :video")
    Long countByVideo(@Param("video") Video video);
} 