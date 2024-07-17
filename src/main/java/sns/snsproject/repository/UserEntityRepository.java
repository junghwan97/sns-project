package sns.snsproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.snsproject.model.entity.UserEntity;

import java.util.Optional;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUserName(String userName);
}