package videoapp.storage.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import videoapp.common.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
