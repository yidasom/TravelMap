package com.travelmap.repository;

import com.travelmap.entity.Video;
import com.travelmap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    
    Optional<Video> findByVideoId(String videoId);
    
    List<Video> findByUser(User user);
    
    List<Video> findByUserId(Long userId);
    
    List<Video> findByProcessedFalse();
    
    List<Video> findByOcrProcessedFalse();
    
    @Query("SELECT v FROM Video v WHERE v.uploadDate BETWEEN :startDate AND :endDate")
    List<Video> findByUploadDateBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT v FROM Video v JOIN v.user u WHERE u.gender = :gender")
    List<Video> findByUserGender(@Param("gender") String gender);
    
    @Query("SELECT v FROM Video v JOIN v.visitCountries vc WHERE vc.countryCode = :countryCode")
    List<Video> findByCountryCode(@Param("countryCode") String countryCode);
    
    @Query("SELECT v FROM Video v JOIN v.user u JOIN v.visitCountries vc " +
       "WHERE (COALESCE(:userId, u.id) = u.id) " +
       "AND (COALESCE(:countryCode, vc.countryCode) = vc.countryCode) " +
       "AND (COALESCE(:gender, u.gender) = u.gender) " +
       "AND (COALESCE(:startDate, v.uploadDate) <= v.uploadDate) " +
       "AND (COALESCE(:endDate, v.uploadDate) >= v.uploadDate)")
    List<Video> findByFilters(@Param("userId") Long userId,
                          @Param("countryCode") String countryCode,
                          @Param("gender") String gender,
                          @Param("startDate") LocalDateTime startDate,
                          @Param("endDate") LocalDateTime endDate);

    
    @Query("SELECT COUNT(v) FROM Video v WHERE v.user = :user")
    Long countByUser(@Param("user") User user);
    
    boolean existsByVideoId(String videoId);
} 