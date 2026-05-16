package com.softwareengineering.petsitter.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import com.softwareengineering.petsitter.offerrequest.domain.RequestStatus;
import com.softwareengineering.petsitter.offerrequest.repository.OfferRequestRepository;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class BookingServiceTest {

    // ── acceptRequest: Erfolgsfall ────────────────────────────────────────

    @Test
    void acceptRequest_createsBookingWithCorrectDataAndSetsOfferBooked() {
        User creator = user(UUID.randomUUID());
        User requester = user(UUID.randomUUID());

        Offer offer = offer(UUID.randomUUID(), creator, OfferStatus.OPEN, OfferType.OWNER_OFFER);
        OfferRequest request = request(UUID.randomUUID(), offer, requester, RequestStatus.PENDING);

        AtomicReference<Booking> savedBooking = new AtomicReference<>();
        AtomicReference<Offer> savedOffer = new AtomicReference<>();
        AtomicReference<OfferRequest> savedRequest = new AtomicReference<>();

        BookingService service = serviceWith(
                requestRepositoryWithRequest(request, savedRequest),
                offerRepositoryCapturing(savedOffer),
                bookingRepositoryCapturing(savedBooking)
        );

        Booking result = service.acceptRequest(request.getId(), creator.getId());

        assertThat(result).isNotNull();
        assertThat(savedBooking.get().getStatus()).isEqualTo(BookingStatus.CREATED);
        assertThat(savedBooking.get().getOwner()).isSameAs(creator);
        assertThat(savedBooking.get().getSitter()).isSameAs(requester);
        assertThat(savedBooking.get().getOffer()).isSameAs(offer);
        assertThat(savedOffer.get().getStatus()).isEqualTo(OfferStatus.BOOKED);

        // Expliziter Persistenz-Nachweis: save(...) wurde mit ACCEPTED aufgerufen.
        assertThat(savedRequest.get()).isSameAs(request);
        assertThat(savedRequest.get().getStatus()).isEqualTo(RequestStatus.ACCEPTED);
    }

    @Test
    void acceptRequest_deniesAllOtherPendingRequests() {
        User creator = user(UUID.randomUUID());
        User requester1 = user(UUID.randomUUID());
        User requester2 = user(UUID.randomUUID());

        Offer offer = offer(UUID.randomUUID(), creator, OfferStatus.OPEN, OfferType.OWNER_OFFER);
        OfferRequest acceptedReq = request(UUID.randomUUID(), offer, requester1, RequestStatus.PENDING);
        OfferRequest otherReq    = request(UUID.randomUUID(), offer, requester2, RequestStatus.PENDING);

        List<OfferRequest> deniedRequests = new ArrayList<>();

        BookingService service = serviceWith(
                requestRepositoryWithAcceptedAndOtherPending(acceptedReq, List.of(otherReq), deniedRequests),
                offerRepositoryCapturing(new AtomicReference<>()),
                bookingRepositoryCapturing(new AtomicReference<>())
        );

        service.acceptRequest(acceptedReq.getId(), creator.getId());

        assertThat(deniedRequests).hasSize(1);
        assertThat(deniedRequests.getFirst().getStatus()).isEqualTo(RequestStatus.DENIED);
    }

    @Test
    void acceptRequest_failsWhenNotOfferCreator() {
        User creator   = user(UUID.randomUUID());
        User requester = user(UUID.randomUUID());
        User intruder  = user(UUID.randomUUID());

        Offer offer = offer(UUID.randomUUID(), creator, OfferStatus.OPEN, OfferType.OWNER_OFFER);
        OfferRequest request = request(UUID.randomUUID(), offer, requester, RequestStatus.PENDING);

        BookingService service = serviceWith(
                requestRepositoryWithRequest(request, new AtomicReference<>()),
                offerRepositoryCapturing(new AtomicReference<>()),
                bookingRepositoryCapturing(new AtomicReference<>())
        );

        assertThatThrownBy(() -> service.acceptRequest(request.getId(), intruder.getId()))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void acceptRequest_failsWhenRequestNotPending() {
        User creator   = user(UUID.randomUUID());
        User requester = user(UUID.randomUUID());

        Offer offer = offer(UUID.randomUUID(), creator, OfferStatus.OPEN, OfferType.OWNER_OFFER);
        OfferRequest request = request(UUID.randomUUID(), offer, requester, RequestStatus.DENIED);

        BookingService service = serviceWith(
                requestRepositoryWithRequest(request, new AtomicReference<>()),
                offerRepositoryCapturing(new AtomicReference<>()),
                bookingRepositoryCapturing(new AtomicReference<>())
        );

        assertThatThrownBy(() -> service.acceptRequest(request.getId(), creator.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void acceptRequest_failsWhenOfferNotOpen() {
        User creator   = user(UUID.randomUUID());
        User requester = user(UUID.randomUUID());

        Offer offer = offer(UUID.randomUUID(), creator, OfferStatus.BOOKED, OfferType.OWNER_OFFER);
        OfferRequest request = request(UUID.randomUUID(), offer, requester, RequestStatus.PENDING);

        BookingService service = serviceWith(
                requestRepositoryWithRequest(request, new AtomicReference<>()),
                offerRepositoryCapturing(new AtomicReference<>()),
                bookingRepositoryCapturing(new AtomicReference<>())
        );

        assertThatThrownBy(() -> service.acceptRequest(request.getId(), creator.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void acceptRequest_failsWhenRequestNotFound() {
        BookingService service = serviceWith(
                requestRepositoryEmpty(),
                offerRepositoryCapturing(new AtomicReference<>()),
                bookingRepositoryCapturing(new AtomicReference<>())
        );

        assertThatThrownBy(() -> service.acceptRequest(UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }

    // ── cancelBooking ────────────────────────────────────────────────────

    @Test
    void cancelBooking_setsStatusCancelled() {
        User owner  = user(UUID.randomUUID());
        User sitter = user(UUID.randomUUID());
        Booking booking = existingBooking(UUID.randomUUID(), owner, sitter, BookingStatus.CREATED);

        AtomicReference<Booking> savedBooking = new AtomicReference<>();

        BookingService service = serviceWith(
                requestRepositoryEmpty(),
                offerRepositoryCapturing(new AtomicReference<>()),
                bookingRepositoryWithBooking(booking, savedBooking)
        );

        service.cancelBooking(booking.getId(), owner.getId());

        assertThat(savedBooking.get().getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelBooking_failsWhenNeitherOwnerNorSitter() {
        User owner    = user(UUID.randomUUID());
        User sitter   = user(UUID.randomUUID());
        User intruder = user(UUID.randomUUID());
        Booking booking = existingBooking(UUID.randomUUID(), owner, sitter, BookingStatus.CREATED);

        BookingService service = serviceWith(
                requestRepositoryEmpty(),
                offerRepositoryCapturing(new AtomicReference<>()),
                bookingRepositoryWithBooking(booking, new AtomicReference<>())
        );

        assertThatThrownBy(() -> service.cancelBooking(booking.getId(), intruder.getId()))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    // ── Domain object factories ──────────────────────────────────────────

    private User user(UUID id) {
        User u = new User();
        u.setId(id);
        u.setEmail(id + "@test.local");
        u.setFirstName("Test");
        u.setLastName("User");
        return u;
    }

    private Offer offer(UUID id, User creator, OfferStatus status, OfferType type) {
        Offer o = new Offer();
        o.setOfferId(id);
        o.setCreator(creator);
        o.setUpdateUser(creator);
        o.setStatus(status);
        o.setType(type);
        o.setStartDate(LocalDate.now());
        o.setEndDate(LocalDate.now().plusDays(7));
        o.setPricePerWeek(new BigDecimal("50.00"));
        return o;
    }

    private OfferRequest request(UUID id, Offer offer, User requester, RequestStatus status) {
        OfferRequest r = new OfferRequest();
        r.setId(id);
        r.setOffer(offer);
        r.setRequester(requester);
        r.setStatus(status);
        return r;
    }

    private Booking existingBooking(UUID id, User owner, User sitter, BookingStatus status) {
        Booking b = new Booking();
        b.setId(id);
        b.setOwner(owner);
        b.setSitter(sitter);
        b.setStatus(status);
        return b;
    }

    // ── Service factory ──────────────────────────────────────────────────

    private BookingService serviceWith(
            OfferRequestRepository requestRepo,
            OfferRepository offerRepo,
            BookingRepository bookingRepo
    ) {
        ApplicationEventPublisher eventPublisher = event -> { };
        return new BookingService(bookingRepo, requestRepo, offerRepo, eventPublisher);
    }

    // ── Repository test doubles ──────────────────────────────────────────

    private OfferRequestRepository requestRepositoryWithAcceptedAndOtherPending(
            OfferRequest request,
            List<OfferRequest> otherPending,
            List<OfferRequest> capturedDenied
    ) {
        return (OfferRequestRepository) Proxy.newProxyInstance(
                OfferRequestRepository.class.getClassLoader(),
                new Class<?>[] {OfferRequestRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.of(request);
                    case "save" -> { var r = (OfferRequest) args[0]; yield r; }
                    case "saveAll" -> {
                        @SuppressWarnings("unchecked")
                        Iterable<OfferRequest> items = (Iterable<OfferRequest>) args[0];
                        items.forEach(capturedDenied::add);
                        yield items;
                    }
                    case "findAllByOfferOfferIdAndStatus" -> otherPending;
                    case "toString" -> "OfferRequestRepositoryTestDouble";
                    default -> throw new UnsupportedOperationException("Not stubbed: " + method.getName());
                }
        );
    }

    private OfferRequestRepository requestRepositoryEmpty() {
        return (OfferRequestRepository) Proxy.newProxyInstance(
                OfferRequestRepository.class.getClassLoader(),
                new Class<?>[] {OfferRequestRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.empty();
                    case "toString" -> "EmptyOfferRequestRepository";
                    default -> throw new UnsupportedOperationException("Not stubbed: " + method.getName());
                }
        );
    }

    private OfferRequestRepository requestRepositoryWithRequest(
            OfferRequest request,
            AtomicReference<OfferRequest> capturedSavedRequest
    ) {
        return (OfferRequestRepository) Proxy.newProxyInstance(
                OfferRequestRepository.class.getClassLoader(),
                new Class<?>[] {OfferRequestRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.of(request);
                    case "save" -> {
                        OfferRequest saved = (OfferRequest) args[0];
                        capturedSavedRequest.set(saved);
                        yield saved;
                    }
                    case "findAllByOfferOfferIdAndStatus" -> List.of();
                    case "saveAll" -> args[0];
                    case "toString" -> "OfferRequestRepositoryRequestOnlyTestDouble";
                    default -> throw new UnsupportedOperationException("Not stubbed: " + method.getName());
                }
        );
    }

    private OfferRepository offerRepositoryCapturing(AtomicReference<Offer> savedRef) {
        return (OfferRepository) Proxy.newProxyInstance(
                OfferRepository.class.getClassLoader(),
                new Class<?>[] {OfferRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "save" -> { var o = (Offer) args[0]; savedRef.set(o); yield o; }
                    case "toString" -> "OfferRepositoryTestDouble";
                    default -> throw new UnsupportedOperationException("Not stubbed: " + method.getName());
                }
        );
    }

    private BookingRepository bookingRepositoryCapturing(AtomicReference<Booking> savedRef) {
        return (BookingRepository) Proxy.newProxyInstance(
                BookingRepository.class.getClassLoader(),
                new Class<?>[] {BookingRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "save" -> { var b = (Booking) args[0]; savedRef.set(b); yield b; }
                    case "findById" -> Optional.empty();
                    case "toString" -> "BookingRepositoryTestDouble";
                    default -> throw new UnsupportedOperationException("Not stubbed: " + method.getName());
                }
        );
    }

    private BookingRepository bookingRepositoryWithBooking(
            Booking booking,
            AtomicReference<Booking> savedRef
    ) {
        return (BookingRepository) Proxy.newProxyInstance(
                BookingRepository.class.getClassLoader(),
                new Class<?>[] {BookingRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.of(booking);
                    case "save" -> { var b = (Booking) args[0]; savedRef.set(b); yield b; }
                    case "toString" -> "BookingRepositoryTestDouble";
                    default -> throw new UnsupportedOperationException("Not stubbed: " + method.getName());
                }
        );
    }
}

