package sns.snsproject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.snsproject.model.entity.PostEntity;
import sns.snsproject.model.entity.UserEntity;

import java.util.List;

@Repository
public interface PostEntityRepository extends JpaRepository<PostEntity, Long> {
    Page<PostEntity> findAllByUser(UserEntity userEntity, Pageable pageable);

    Page<PostEntity> findAllByOrderByRegisteredAtDesc(Pageable pageable);

    List<PostEntity> findByUserIn(List<UserEntity> users);
}
