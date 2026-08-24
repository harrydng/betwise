package com.betwise.repository;

import java.util.List;

import org.springframework.boot.data.autoconfigure.web.DataWebProperties.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.betwise.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // example query for when to use repository
    @Query("""
                SELECT p
                FROM Profile p
                WHERE p.experienceLevel = :level
                ORDER BY p.id DESC
            """)
    List<Profile> findProfilesForSearch(
            @Param("level") String level,
            Pageable pageable);
}
