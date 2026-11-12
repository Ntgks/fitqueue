package dev.fitqueue.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import dev.fitqueue.dto.CreateMemberRequest;
import dev.fitqueue.dto.MemberResponse;
import dev.fitqueue.model.Member;
import dev.fitqueue.repository.MemberRepository;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;
import org.mockito.ArgumentCaptor;

class MemberServiceTest {
    
    @Test 
    void shouldCreateMember(){
        MemberRepository repository = mock(MemberRepository.class);

        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        MemberService service = new MemberService(repository, passwordEncoder);

        CreateMemberRequest request = new CreateMemberRequest(
            "Vaggelis",
            "vaggelis@example.com",
            "test-password-123"
        );

        when(repository.save(any(Member.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(passwordEncoder.encode("test-password-123"))
            .thenReturn("encoded-password");

        MemberResponse response = service.createMember(request);

        assertEquals("Vaggelis", response.name());
        assertEquals("vaggelis@example.com", response.email());

        verify(passwordEncoder).encode("test-password-123");

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        verify(repository).save(captor.capture());

        Member savedMember = captor.getValue();

        assertEquals("encoded-password", savedMember.getPasswordHash());
    }

    @Test 
    void shouldRejectPasswordExceeding72Bytes() {
        MemberRepository repository = mock(MemberRepository.class);

        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        MemberService service = new MemberService(repository, passwordEncoder);

        CreateMemberRequest request = new CreateMemberRequest(
            "Vaggelis",
            "vaggelis@example.com",
            "α".repeat(37)
        );

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, 
            () -> service.createMember(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(passwordEncoder, repository);
    }
}
