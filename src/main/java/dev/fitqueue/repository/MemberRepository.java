package dev.fitqueue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import dev.fitqueue.model.Member;

public interface MemberRepository extends JpaRepository<Member, Long>{
}