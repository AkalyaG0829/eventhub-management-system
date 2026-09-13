package com.eventhub.repository;

import com.eventhub.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findBySubmissionId(Long submissionId);
    boolean existsBySubmissionIdAndJudgeId(Long submissionId, Long judgeId);
}
