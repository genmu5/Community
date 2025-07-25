package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Comment;
import month.communitybackend.domain.Post;
import month.communitybackend.domain.User;
import month.communitybackend.repository.CommentRepository;
import month.communitybackend.repository.PostRepository;
import month.communitybackend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepo;
    private final PostRepository postRepo;
    private final UserRepository userRepo;

    // 특정 게시글에 대한 댓글 생성
    @Transactional
    public Comment create(Long postId, String content) {
        // 현재 인증된 사용자 정보 조회
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다: " + username));

        // 댓글을 작성할 게시글 정보 조회
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글을 찾을 수 없습니다: " + postId));

        // 댓글 객체 생성
        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(content)
                .build();

        // 생성된 댓글을 DB에 저장
        return commentRepo.save(comment);
    }

    // 특정 게시글에 달린 모든 댓글 조회
    @Transactional(readOnly = true)
    public List<Comment> listByPost(Long postId) {
        return commentRepo.findByPostId(postId);
    }

    // 댓글 삭제
    @Transactional
    public void delete(Long postId, Long commentId) {
        // 삭제할 댓글 정보 조회
        Comment comment = commentRepo.findById(commentId)
                        .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        // 댓글이 대상 게시글에 속해 있는지 확인
        if(!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 작성되지 않은 댓글입니다.");
        }

        // 댓글 삭제 실행
        commentRepo.deleteById(commentId);
    }

    // 단일 댓글 정보 조회
    @Transactional(readOnly = true)
    public Comment get(Long id) {
        return commentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다. id =" + id));
    }

    // 댓글 내용 수정
    @Transactional
    public Comment update(Long postId, Long id, String newContent) {
        //  수정할 댓글 정보 조회
        Comment c = get(id);

        // 댓글이 대상 게시글에 속해 있는지 확인
        if(!c.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 작성되지 않은 댓글입니다.");
        }

        // 댓글 내용 변경
        c.setContent(newContent);

        // 변경된 내용을 DB에 저장
        return commentRepo.save(c);
    }
}