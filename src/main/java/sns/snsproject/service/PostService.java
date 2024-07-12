package sns.snsproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.snsproject.exception.ErrorCode;
import sns.snsproject.exception.SnsApplicationException;
import sns.snsproject.model.entity.PostEntity;
import sns.snsproject.model.entity.UserEntity;
import sns.snsproject.repository.PostEntityRepository;
import sns.snsproject.repository.UserEntityRepository;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostEntityRepository postEntityRepository;
    private final UserEntityRepository userEntityRepository;


    @Transactional
    public void create(String title, String body, String userName) {
        // user 조회 + user가 없다면
        UserEntity userEntity = getUserEntityOrException(userName);
        // post save
        postEntityRepository.save(PostEntity.of(title, body, userEntity));
    }
        // user exist
    private UserEntity getUserEntityOrException(String userName) {
        return userEntityRepository.findByUserName(userName).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", userName)));
    }


}
