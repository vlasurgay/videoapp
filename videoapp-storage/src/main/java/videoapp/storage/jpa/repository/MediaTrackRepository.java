package videoapp.storage.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import videoapp.common.model.entity.MediaTrack;

import java.util.List;

public interface MediaTrackRepository extends JpaRepository<MediaTrack, Long> {

    List<MediaTrack> findByVideoId(Long videoId);
}
