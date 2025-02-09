package ru.skillbox.social_network_post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skillbox.social_network_post.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {


    Page<Post> findAll(Pageable pageable);
}