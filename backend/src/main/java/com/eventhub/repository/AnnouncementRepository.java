package com.eventhub.repository;

import com.eventhub.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByEventIdOrderByCreatedAtDesc(Long eventId);
}
