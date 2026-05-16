package com.softwareengineering.petsitter.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.chat.domain.ChatConversationDocument;
import com.softwareengineering.petsitter.chat.repository.ChatConversationRepository;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ChatAccessServiceTest {

    @Test
    void verifyAccess_allowsOwnerOrSitter() {
        ChatConversationRepository conversationRepository = Mockito.mock(ChatConversationRepository.class);
        BookingRepository bookingRepository = Mockito.mock(BookingRepository.class);
        ChatAccessService service = new ChatAccessService(conversationRepository, bookingRepository);

        UUID ownerId = UUID.randomUUID();
        UUID sitterId = UUID.randomUUID();
        ChatConversationDocument conversation = new ChatConversationDocument();
        conversation.setId("conv-1");
        conversation.setOwnerId(ownerId);
        conversation.setSitterId(sitterId);

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conversation));

        ChatConversationDocument forOwner = service.verifyAccess("conv-1", ownerId);
        ChatConversationDocument forSitter = service.verifyAccess("conv-1", sitterId);

        assertThat(forOwner.getId()).isEqualTo("conv-1");
        assertThat(forSitter.getId()).isEqualTo("conv-1");
    }

    @Test
    void verifyAccess_rejectsForeignUser() {
        ChatConversationRepository conversationRepository = Mockito.mock(ChatConversationRepository.class);
        BookingRepository bookingRepository = Mockito.mock(BookingRepository.class);
        ChatAccessService service = new ChatAccessService(conversationRepository, bookingRepository);

        ChatConversationDocument conversation = new ChatConversationDocument();
        conversation.setId("conv-2");
        conversation.setOwnerId(UUID.randomUUID());
        conversation.setSitterId(UUID.randomUUID());

        when(conversationRepository.findById("conv-2")).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> service.verifyAccess("conv-2", UUID.randomUUID()))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void verifyBookingAccess_allowsOwnerOrSitter() {
        ChatConversationRepository conversationRepository = Mockito.mock(ChatConversationRepository.class);
        BookingRepository bookingRepository = Mockito.mock(BookingRepository.class);
        ChatAccessService service = new ChatAccessService(conversationRepository, bookingRepository);

        UUID ownerId = UUID.randomUUID();
        UUID sitterId = UUID.randomUUID();

        Booking booking = new Booking();
        User owner = new User();
        owner.setId(ownerId);
        User sitter = new User();
        sitter.setId(sitterId);
        booking.setOwner(owner);
        booking.setSitter(sitter);

        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThat(service.verifyBookingAccess(bookingId, ownerId)).isSameAs(booking);
        assertThat(service.verifyBookingAccess(bookingId, sitterId)).isSameAs(booking);
    }

    @Test
    void verifyBookingAccess_rejectsUnknownBooking() {
        ChatConversationRepository conversationRepository = Mockito.mock(ChatConversationRepository.class);
        BookingRepository bookingRepository = Mockito.mock(BookingRepository.class);
        ChatAccessService service = new ChatAccessService(conversationRepository, bookingRepository);

        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyBookingAccess(bookingId, UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }
}

