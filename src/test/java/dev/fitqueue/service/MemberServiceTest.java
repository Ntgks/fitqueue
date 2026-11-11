package dev.fitqueue.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import dev.fitqueue.dto.CreateMemberRequest;
import dev.fitqueue.model.Member;
import dev.fitqueue.repository.MemberRepository;
import static org.mockito.Mockito.verify;

class MemberServiceTest {
    
    @Test 
    void shouldCreateMember(){
        MemberRepository repository = mock(MemberRepository.class);

        MemberService service = new MemberService(repository);

        CreateMemberRequest request = 
            new CreateMemberRequest("Vaggelis", "vaggelis@example.com");

        when(repository.save(any(Member.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Member member = service.createMember(request);

        assertEquals("Vaggelis", member.getName());
        assertEquals("vaggelis@example.com", member.getEmail());

        verify(repository).save(member);

    }
}
