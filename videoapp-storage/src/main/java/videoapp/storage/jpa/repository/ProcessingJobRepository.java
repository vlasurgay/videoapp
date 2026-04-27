package videoapp.storage.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import videoapp.common.model.entity.ProcessingJob;

import java.util.Optional;

public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, Long> {


    @Transactional
    @Query(value = """
            update processing_jobs pj
            set status = 'PROCESSING', attempt = pj.attempt + 1, updated_at = now()
            from (
                select pending_pj.id
                from processing_jobs pending_pj
                where (pending_pj.status = 'PENDING' or (pending_pj.status = 'RETRY_WAIT' and pending_pj.next_retry_at < now()))
                and not exists (
                    select 1
                    from job_dependencies jd
                    join processing_jobs dep on dep.id = jd.depends_on_job_id
                    where jd.dependent_job_id = pending_pj.id
                    and dep.status != 'COMPLETED'
                )
                order by pending_pj.created_at
                limit 1
                for update skip locked
            ) picked
            where pj.id = picked.id
            returning pj.*;
    """, nativeQuery = true)
    Optional<ProcessingJob> pickNextJob();

}
