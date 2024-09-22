package sns.snsproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sns.snsproject.controller.request.PostCommentRequest;
import sns.snsproject.controller.request.PostCreateRequest;
import sns.snsproject.controller.request.PostModifyRequest;
import sns.snsproject.controller.response.CommentResponse;
import sns.snsproject.controller.response.PostResponse;
import sns.snsproject.controller.response.Response;
import sns.snsproject.model.Post;
import sns.snsproject.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping()
    public Response<Void> create(@RequestBody PostCreateRequest request, Authentication authentication) {
        postService.create(request.getTitle(), request.getBody(), authentication.getName());
        return Response.success();
    }

    @PutMapping("/{postId}")
    public Response<PostResponse> modify(@PathVariable Long postId,
                                         @RequestBody PostModifyRequest request, Authentication authentication) {
        Post post = postService.modify(request.getTitle(), request.getBody(), authentication.getName(), postId);
        return Response.success(PostResponse.fromPost(post));
    }

    @DeleteMapping("/{postId}")
    public Response<Void> delete(@PathVariable Long postId, Authentication authentication) {
        postService.delete(authentication.getName(), postId);
        return Response.success();
    }

    @GetMapping("/followed")
    public Response<List<PostResponse>> getFeed(@RequestParam(value = "cursorId", defaultValue = "0") Long cursorId,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                Authentication authentication) {
        return Response.success(postService.getPostsFromFollowedUsers(authentication.getName(), size, cursorId));
    }

    @GetMapping("/{postId}")
    public Response<PostResponse> getPost(@PathVariable Long postId) {
        return Response.success(PostResponse.fromPost(postService.selectById(postId)));
    }

    @GetMapping("/popular")
    public Response<List<PostResponse>> getTopPosts() {
        List<PostResponse> topPosts = postService.getTopPosts(5);
        return Response.success(topPosts);
    }

    @GetMapping("/my")
    public Response<Page<PostResponse>> my(Pageable pageable, Authentication authentication) {
        return Response.success(postService.my(authentication.getName(), pageable).map(PostResponse::fromPost));
    }

    @PostMapping("/{postId}/likes")
    public Response<Void> like(@PathVariable Long postId, Authentication authentication) {
        postService.like(postId, authentication.getName());
        return Response.success();
    }

    @GetMapping("/{postId}/likes")
    public Response<Long> getCount(@PathVariable Long postId) {
        return Response.success(postService.likeCount(postId));
    }

    @PostMapping("/{postId}/comments")
    public Response<Void> comment(@PathVariable Long postId,
                                  @RequestBody PostCommentRequest request,
                                  Authentication authentication) {
        postService.comment(postId, request.getComment(), authentication.getName());
        return Response.success();
    }

    @GetMapping("/{postId}/comments")
    public Response<Page<CommentResponse>> commentList(@PathVariable Long postId, Pageable pageable, Authentication authentication) {
        return Response.success(postService.getComment(postId, pageable).map(CommentResponse::fromComment));
    }
}
