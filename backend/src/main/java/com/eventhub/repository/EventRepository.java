package com.eventhub.repository;

import com.eventhub.model.Event;
import com.eventhub.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"organizer"})
    org.springframework.data.domain.Page<Event> findAll(org.springframework.data.jpa.domain.Specification<Event> spec, org.springframework.data.domain.Pageable pageable);
    
    List<Event> findByStatus(EventStatus status);
    List<Event> findByOrganizerId(Long organizerId);
    long countByStatus(EventStatus status);
}
