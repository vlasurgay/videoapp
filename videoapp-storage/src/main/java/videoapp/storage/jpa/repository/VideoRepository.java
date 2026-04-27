package videoapp.storage.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import videoapp.common.model.entity.Video;
import videoapp.common.model.enums.VideoStatus;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {

    @Modifying
    @Transactional
    @Query("update Video v set v.status = :status " +
            "where v.id = (select u.videoId from UploadInfo u where u.uploadId = :uploadId)")
    int updateStatusByUploadId(@Param("uploadId") String uploadId, @Param("status") VideoStatus status);

    @Modifying
    @Transactional
    @Query("update Video v set v.sourceVideoKey = :sourceVideoKey where v.publicId = :publicId")
    void updateSourceVideoKeyByPublicId(@Param("publicId") String publicId, @Param("sourceVideoKey") String sourceVideoKey);

    @Modifying
    @Transactional
    @Query("update Video v set v.sourceAudioKey = :sourceAudioKey where v.publicId = :publicId")
    void updateSourceAudioKeyByPublicId(@Param("publicId") String publicId, @Param("sourceAudioKey") String sourceAudioKey);

    @Modifying
    @Transactional
    @Query("update Video v set v.masterPlaylistKey = :masterPlaylistKey where v.publicId = :publicId")
    void updateMasterPlaylistKeyByPublicId(@Param("publicId") String publicId, @Param("masterPlaylistKey") String masterPlaylistKey);

    Optional<Video> findByPublicId(String publicId);
}
