package videoapp.storage.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import videoapp.common.model.entity.UploadInfo;

import java.util.Optional;

public interface UploadInfoRepository extends JpaRepository<UploadInfo, Long> {

    Optional<UploadInfo> findByVideoId(@Param("videoId") Long videoId);
}
