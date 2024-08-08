package sns.snsproject.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sns.snsproject.exception.ErrorCode;
import sns.snsproject.exception.SnsApplicationException;
import sns.snsproject.model.entity.PostEntity;
import sns.snsproject.model.entity.UserEntity;
import sns.snsproject.repository.PostEntityRepository;
import sns.snsproject.repository.UserEntityRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostEntityRepository postEntityRepository;

    @Mock
    private UserEntityRepository userEntityRepository;

    @Test
    void 포스트작성이_성공한경우() {
        String title = "title";
        String body = "body";
        String userName = "userName";

        // mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(mock(UserEntity.class)));
        when(postEntityRepository.save(any())).thenReturn(mock(PostEntity.class));

        Assertions.assertDoesNotThrow(()->postService.create(title, body, userName));
    }

    @Test
    void 포스트작성시_요청한유저가_존재하지않는경우() {
        String title = "title";
        String body = "body";
        String userName = "userName";

        // mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.empty());
//        when(postEntityRepository.save(any())).thenReturn(mock(PostEntity.class));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, ()->postService.create(title, body, userName));
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());

    }

    @Test
    void 포스트수정이_성공한경우() {

        String title = "title";
        String body = "body";
        String userName = "userName";
        Long postId = 1L;


        UserEntity userEntity = new UserEntity();
        userEntity.setUserName(userName);
        userEntity.setId(1L);

        PostEntity postEntity = new PostEntity();
        postEntity.setId(postId);
        postEntity.setUser(userEntity);

        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(userEntity));
        when(postEntityRepository.findById(postId)).thenReturn(Optional.of(postEntity));
        when(postEntityRepository.saveAndFlush(any())).thenReturn(postEntity);

        Assertions.assertDoesNotThrow(()-> postService.modify(title, body, userName, postId));
    }

    @Test
    void 포스트수정시_포스트가_없으면_예외를_발생한다() {
        String title = "title";
        String body = "body";
        String userName = "userName";

        // mocking
        UserEntity mockUserEntity = new UserEntity();

        when(userEntityRepository.findByUserName(any())).thenReturn(Optional.of(mockUserEntity));
        when(postEntityRepository.findById(any())).thenReturn(Optional.empty());
//        when(postEntityRepository.save(any())).thenReturn(mock(PostEntity.class));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, ()->postService.modify(title, body, userName, 1L));
        Assertions.assertEquals(ErrorCode.POST_NOT_FOUND, e.getErrorCode());

    }

    @Test
    void 포스트수정시_작성한_유저가_다르면_예외를_발생한다() {
        String title = "title";
        String body = "body";
        String userName = "userName";
        String wrongName = "wrongName";
        Long postId = 1L;

        UserEntity mockUserEntity = new UserEntity();
        mockUserEntity.setUserName(userName);
        mockUserEntity.setId(1L);

        UserEntity diffUserEntity = new UserEntity();
        mockUserEntity.setUserName(wrongName);
        mockUserEntity.setId(2L);

        PostEntity mockPostEntity = new PostEntity();
        mockPostEntity.setId(postId);
        mockPostEntity.setUser(diffUserEntity);

        // mocking
        when(userEntityRepository.findByUserName(any())).thenReturn(Optional.of(mockUserEntity));
        when(postEntityRepository.findById(any())).thenReturn(Optional.of(mockPostEntity));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, ()->postService.modify(title, body, userName, 1L));
        Assertions.assertEquals(ErrorCode.INVALID_PERMISSION, e.getErrorCode());

    }

    @Test
    void 포스트삭제가_성공한경우() {

        String userName = "userName";
        Long postId = 1L;

        UserEntity mockUserEntity = new UserEntity();
        mockUserEntity.setUserName(userName);
        mockUserEntity.setId(1L);

        PostEntity mockPostEntity = new PostEntity();
        mockPostEntity.setId(postId);
        mockPostEntity.setUser(mockUserEntity);

        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(mockUserEntity));
        when(postEntityRepository.findById(postId)).thenReturn(Optional.of(mockPostEntity));

        Assertions.assertDoesNotThrow(()-> postService.delete(userName, 1L));
    }

    @Test
    void 포스트삭제시_포스트가_존재하지_않는경우() {
        String userName = "userName";
        Long postId = 1L;

        UserEntity mockUserEntity = new UserEntity();
        mockUserEntity.setUserName(userName);
        mockUserEntity.setId(1L);

        PostEntity mockPostEntity = new PostEntity();
        mockPostEntity.setId(postId);
        mockPostEntity.setUser(mockUserEntity);

        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(mockUserEntity));
        when(postEntityRepository.findById(postId)).thenReturn(Optional.empty());

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> postService.delete(userName, 1L));
        Assertions.assertEquals(ErrorCode.POST_NOT_FOUND, e.getErrorCode());
    }

    @Test
    void 포스트삭제시_작성자와_유저가_다른경우() {
        String userName = "userName";
        String wrongName = "wrongName";
        Long postId = 1L;

        UserEntity mockUserEntity = new UserEntity();
        mockUserEntity.setUserName(userName);
        mockUserEntity.setId(1L);

        UserEntity diffUserEntity = new UserEntity();
        mockUserEntity.setUserName(wrongName);
        mockUserEntity.setId(2L);

        PostEntity mockPostEntity = new PostEntity();
        mockPostEntity.setId(postId);
        mockPostEntity.setUser(mockUserEntity);

        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(diffUserEntity));
        when(postEntityRepository.findById(postId)).thenReturn(Optional.of(mockPostEntity));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> postService.delete(userName, 1L));
        Assertions.assertEquals(ErrorCode.INVALID_PERMISSION, e.getErrorCode());
    }

}
