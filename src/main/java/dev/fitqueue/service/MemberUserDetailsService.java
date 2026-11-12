package dev.fitqueue.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import dev.fitqueue.model.Member;

import dev.fitqueue.repository.MemberRepository;
import org.springframework.security.core.userdetails.User;


@Service
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public MemberUserDetailsService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {

            Member member = memberRepository.findByEmail(username)
                .orElseThrow(() ->
                new UsernameNotFoundException("Invalid credentials")
            );

            if (member.getPasswordHash() == null ||
                member.getPasswordHash().isBlank()) {
                    throw new UsernameNotFoundException("Invalid credentials");
            }

            return User.withUsername(member.getEmail())
                .password(member.getPasswordHash())
                .roles(member.getRole().name())
                .build();

    }
}
