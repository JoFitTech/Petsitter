package com.softwareengineering.petsitter.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.chat.domain.ChatConversationDocument;
import com.softwareengineering.petsitter.chat.domain.ChatMessageDocument;
import com.softwareengineering.petsitter.chat.dto.ChatConversationDto;
import com.softwareengineering.petsitter.chat.dto.ChatMessageDto;
import com.softwareengineering.petsitter.chat.repository.ChatConversationRepository;
import com.softwareengineering.petsitter.chat.repository.ChatMessageRepository;
import com.softwareengineering.petsitter.notification.service.NotificationService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ChatServiceTest {

    private ChatConversationRepository conversationRepository;
    private ChatMessageRepository messageRepository;
    private BookingRepository bookingRepository;
    private ChatAccessService accessService;
    private ChatEventBus eventBus;
    private NotificationService notificationService;
    private AuthenticatedUser authenticatedUser;
    private UserRepository userRepository;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        conversationRepository = Mockito.mock(ChatConversationRepository.class);
        messageRepository = Mockito.mock(ChatMessageRepository.class);
        bookingRepository = Mockito.mock(BookingRepository.class);
        accessService = Mockito.mock(ChatAccessService.class);
        eventBus = Mockito.mock(ChatEventBus.class);
        notificationService = Mockito.mock(NotificationService.class);
        authenticatedUser = Mockito.mock(AuthenticatedUser.class);
        userRepository = Mockito.mock(UserRepository.class);

        chatService = new ChatService(
                conversationRepository,
                messageRepository,
                bookingRepository,
                accessService,
                eventBus,
                notificationService,
                authenticatedUser,
                userRepository
        );
    }

    @Test
    void createConversationForBooking_createsConversation() {
        UUID bookingId = UUID.randomUUID();
        User owner = user("Anna", "Owner");
        User sitter = user("Ben", "Sitter");

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setOwner(owner);
        booking.setSitter(sitter);

        when(conversationRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(conversationRepository.save(any(ChatConversationDocument.class))).thenAnswer(invocation -> {
            ChatConversationDocument doc = invocation.getArgument(0);
            doc.setId("conv-created");
            return doc;
        });

        ChatConversationDto dto = chatService.createConversationForBooking(bookingId);

        assertThat(dto.conversationId()).isEqualTo("conv-created");
        assertThat(dto.bookingId()).isEqualTo(bookingId);
        assertThat(dto.ownerId()).isEqualTo(owner.getId());
        assertThat(dto.sitterId()).isEqualTo(sitter.getId());
    }

    @Test
    void createConversationForBooking_isIdempotent() {
        UUID bookingId = UUID.randomUUID();
        ChatConversationDocument existing = new ChatConversationDocument();
        existing.setId("conv-existing");
        existing.setBookingId(bookingId);
        existing.setOwnerId(UUID.randomUUID());
        existing.setSitterId(UUID.randomUUID());

        when(conversationRepository.findByBookingId(bookingId)).thenReturn(Optional.of(existing));

        ChatConversationDto dto = chatService.createConversationForBooking(bookingId);

        assertThat(dto.conversationId()).isEqualTo("conv-existing");
        verify(conversationRepository, never()).save(any(ChatConversationDocument.class));
        verify(bookingRepository, never()).findById(any(UUID.class));
    }

    @Test
    void sendMessage_persistsMessage() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = userWithId(currentUserId, "Anna", "Owner");
        when(authenticatedUser.get()).thenReturn(Optional.of(currentUser));

        ChatConversationDocument conversation = new ChatConversationDocument();
        conversation.setId("conv-1");
        conversation.setBookingId(UUID.randomUUID());
        conversation.setOwnerId(currentUserId);
        conversation.setSitterId(UUID.randomUUID());
        when(accessService.verifyAccess("conv-1", currentUserId)).thenReturn(conversation);

        Booking booking = new Booking();
        booking.setOwner(currentUser);
        User recipient = user("Ben", "Sitter");
        recipient.setId(conversation.getSitterId());
        booking.setSitter(recipient);
        when(accessService.verifyBookingAccess(conversation.getBookingId(), currentUserId)).thenReturn(booking);

        when(messageRepository.save(any(ChatMessageDocument.class))).thenAnswer(invocation -> {
            ChatMessageDocument saved = invocation.getArgument(0);
            saved.setId("msg-1");
            return saved;
        });

        ChatMessageDto dto = chatService.sendMessage("conv-1", "Hallo aus dem Test");

        assertThat(dto.messageId()).isEqualTo("msg-1");
        assertThat(dto.conversationId()).isEqualTo("conv-1");
        assertThat(dto.message()).isEqualTo("Hallo aus dem Test");
        verify(notificationService).createChatMessageNotification(eq(recipient), eq(currentUser), eq("conv-1"));
        verify(eventBus).publish(any(ChatMessageDto.class));
    }

    @Test
    void sendMessage_updatesConversationPreview() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = userWithId(currentUserId, "Anna", "Owner");
        when(authenticatedUser.get()).thenReturn(Optional.of(currentUser));

        ChatConversationDocument conversation = new ChatConversationDocument();
        conversation.setId("conv-2");
        conversation.setBookingId(UUID.randomUUID());
        conversation.setOwnerId(currentUserId);
        conversation.setSitterId(UUID.randomUUID());
        when(accessService.verifyAccess("conv-2", currentUserId)).thenReturn(conversation);

        Booking booking = new Booking();
        booking.setOwner(currentUser);
        User recipient = user("Ben", "Sitter");
        recipient.setId(conversation.getSitterId());
        booking.setSitter(recipient);
        when(accessService.verifyBookingAccess(conversation.getBookingId(), currentUserId)).thenReturn(booking);

        when(messageRepository.save(any(ChatMessageDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String longText = "x".repeat(120);
        chatService.sendMessage("conv-2", longText);

        verify(conversationRepository).save(any(ChatConversationDocument.class));
        assertThat(conversation.getLastMessageAt()).isNotNull();
        assertThat(conversation.getLastMessagePreview()).hasSize(103);
        assertThat(conversation.getLastMessagePreview()).endsWith("...");
    }

    @Test
    void sendMessage_createsNotification_viaUserFallbackWhenBookingLookupFails() {
        UUID currentUserId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        User currentUser = userWithId(currentUserId, "Anna", "Owner");
        User recipient = userWithId(recipientId, "Ben", "Sitter");
        when(authenticatedUser.get()).thenReturn(Optional.of(currentUser));

        ChatConversationDocument conversation = new ChatConversationDocument();
        conversation.setId("conv-fallback");
        conversation.setBookingId(UUID.randomUUID());
        conversation.setOwnerId(currentUserId);
        conversation.setSitterId(recipientId);
        when(accessService.verifyAccess("conv-fallback", currentUserId)).thenReturn(conversation);
        when(accessService.verifyBookingAccess(conversation.getBookingId(), currentUserId))
            .thenThrow(new NotFoundException("Booking nicht gefunden"));

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));
        when(messageRepository.save(any(ChatMessageDocument.class))).thenAnswer(invocation -> {
            ChatMessageDocument saved = invocation.getArgument(0);
            saved.setId("msg-fallback");
            return saved;
        });

        ChatMessageDto dto = chatService.sendMessage("conv-fallback", "Fallback Nachricht");

        assertThat(dto.messageId()).isEqualTo("msg-fallback");
        verify(notificationService).createChatMessageNotification(eq(recipient), eq(currentUser), eq("conv-fallback"));
        verify(eventBus).publish(any(ChatMessageDto.class));
    }

    @Test
    void markConversationAsRead_marksMessagesAndNotifications() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = userWithId(currentUserId, "Anna", "Owner");
        when(authenticatedUser.get()).thenReturn(Optional.of(currentUser));

        ChatConversationDocument conversation = new ChatConversationDocument();
        conversation.setId("conv-read");
        conversation.setOwnerId(currentUserId);
        conversation.setSitterId(UUID.randomUUID());
        when(accessService.verifyAccess("conv-read", currentUserId)).thenReturn(conversation);

        ChatMessageDocument m1 = new ChatMessageDocument();
        m1.setId("m1");
        m1.setRead(false);
        ChatMessageDocument m2 = new ChatMessageDocument();
        m2.setId("m2");
        m2.setRead(false);

        when(messageRepository.findAllByConversationIdAndRecipientIdAndReadFalse("conv-read", currentUserId))
                .thenReturn(List.of(m1, m2));

        chatService.markConversationAsRead("conv-read");

        assertThat(m1.isRead()).isTrue();
        assertThat(m2.isRead()).isTrue();
        verify(messageRepository, times(2)).save(any(ChatMessageDocument.class));
        verify(notificationService).markChatNotificationsAsRead("conv-read", currentUserId);
    }

    @Test
    void sendMessage_rejectsEmptyText() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = userWithId(currentUserId, "Anna", "Owner");
        when(authenticatedUser.get()).thenReturn(Optional.of(currentUser));

        ChatConversationDocument conversation = new ChatConversationDocument();
        conversation.setId("conv-empty");
        conversation.setOwnerId(currentUserId);
        conversation.setSitterId(UUID.randomUUID());
        when(accessService.verifyAccess("conv-empty", currentUserId)).thenReturn(conversation);

        assertThatThrownBy(() -> chatService.sendMessage("conv-empty", "   "))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private User user(String firstName, String lastName) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private User userWithId(UUID id, String firstName, String lastName) {
        User user = user(firstName, lastName);
        user.setId(id);
        return user;
    }
}

