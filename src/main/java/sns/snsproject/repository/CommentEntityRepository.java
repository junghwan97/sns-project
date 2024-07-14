package sns.snsproject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.snsproject.model.entity.CommentEntity;
import sns.snsproject.model.entity.PostEntity;

@Repository
public interface CommentEntityRepository extends JpaRepository<CommentEntity, Long> {


    Page<CommentEntity> findAllByPost(PostEntity postEntity, Pageable pageable);
}
