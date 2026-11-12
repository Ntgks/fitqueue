package dev.fitqueue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import dev.fitqueue.model.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>{
    Optional<Member> findByEmail(String email);
}