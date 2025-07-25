package com.travelmap.repository;

import com.travelmap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByYoutubeChannelId(String youtubeChannelId);
    
    List<User> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT u FROM User u WHERE u.totalVideoCount > :minVideoCount")
    List<User> findUsersWithMinimumVideos(@Param("minVideoCount") Long minVideoCount);
    
    boolean existsByYoutubeChannelId(String youtubeChannelId);
} 