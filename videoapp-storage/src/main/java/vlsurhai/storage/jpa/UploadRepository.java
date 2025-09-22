package vlsurhai.storage.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vlsurhai.common.model.video.UploadInfo;
import vlsurhai.common.model.video.UploadStatus;

@Repository
public interface UploadRepository extends JpaRepository<UploadInfo, Long> {

    @Modifying
    @Transactional
    @Query("update UploadInfo ui set ui.status = :uploadStatus where ui.uploadId = :uploadId")
    int updateStatusByUploadId(@Param("uploadId") String uploadId, @Param("uploadStatus") UploadStatus uploadStatus);

}
