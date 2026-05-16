package com.softwareengineering.petsitter.offerrequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import com.softwareengineering.petsitter.offerrequest.domain.RequestStatus;
import com.softwareengineering.petsitter.offerrequest.repository.OfferRequestRepository;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.DuplicateRequestException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class RequestServiceTest {

    @Test
    void createRequest_savesPendingRequestWithTrimmedMessage() {
        UUID offerId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Offer offer = offer(offerId, creatorId, OfferStatus.OPEN);
        User requester = user(requesterId);
        AtomicReference<OfferRequest> savedRequest = new AtomicReference<>();

        RequestService requestService = new RequestService(
                offerRequestRepository(savedRequest, Optional.empty(), new ArrayList<>(), new ArrayList<>()),
                offerRepository(Optional.of(offer)),
                userRepository(Optional.of(requester))
        );

        OfferRequest result = requestService.createRequest(offerId, requesterId, "  Hallo, ich habe Interesse.  ");

        assertThat(savedRequest.get()).isSameAs(result);
        assertThat(result.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(result.getOffer()).isSameAs(offer);
        assertThat(result.getRequester()).isSameAs(requester);
        assertThat(result.getMessage()).isEqualTo("Hallo, ich habe Interesse.");
    }

    @Test
    void createRequest_rejectsSelfRequest() {
        UUID offerId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        Offer offer = offer(offerId, creatorId, OfferStatus.OPEN);

        RequestService requestService = new RequestService(
                offerRequestRepository(new AtomicReference<>(), Optional.empty(), new ArrayList<>(), new ArrayList<>()),
                offerRepository(Optional.of(offer)),
                userRepository(Optional.of(user(creatorId)))
        );

        assertThatThrownBy(() -> requestService.createRequest(offerId, creatorId, "test"))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void createRequest_rejectsDuplicateRequest() {
        UUID offerId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Offer offer = offer(offerId, creatorId, OfferStatus.OPEN);

        RequestService requestService = new RequestService(
                offerRequestRepository(new AtomicReference<>(), Optional.of(new OfferRequest()), new ArrayList<>(), new ArrayList<>()),
                offerRepository(Optional.of(offer)),
                userRepository(Optional.of(user(requesterId)))
        );

        assertThatThrownBy(() -> requestService.createRequest(offerId, requesterId, "test"))
                .isInstanceOf(DuplicateRequestException.class);
    }

    @Test
    void cancelRequest_setsCancelledStatusForPendingRequest() {
        UUID requestId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        OfferRequest existing = new OfferRequest();
        existing.setId(requestId);
        existing.setRequester(user(requesterId));
        existing.setStatus(RequestStatus.PENDING);

        AtomicReference<OfferRequest> savedRequest = new AtomicReference<>();
        RequestService requestService = new RequestService(
                offerRequestRepository(savedRequest, Optional.empty(), List.of(existing), new ArrayList<>()),
                offerRepository(Optional.empty()),
                userRepository(Optional.empty())
        );

        requestService.cancelRequest(requestId, requesterId);

        assertThat(existing.getStatus()).isEqualTo(RequestStatus.CANCELLED);
        assertThat(savedRequest.get()).isSameAs(existing);
    }

    @Test
    void cancelRequest_rejectsInvalidStatus() {
        UUID requestId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        OfferRequest existing = new OfferRequest();
        existing.setId(requestId);
        existing.setRequester(user(requesterId));
        existing.setStatus(RequestStatus.DENIED);

        RequestService requestService = new RequestService(
                offerRequestRepository(new AtomicReference<>(), Optional.empty(), List.of(existing), new ArrayList<>()),
                offerRepository(Optional.empty()),
                userRepository(Optional.empty())
        );

        assertThatThrownBy(() -> requestService.cancelRequest(requestId, requesterId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void findRequestsForOffer_rejectsNonCreator() {
        UUID offerId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID foreignUserId = UUID.randomUUID();
        Offer offer = offer(offerId, creatorId, OfferStatus.OPEN);

        RequestService requestService = new RequestService(
                offerRequestRepository(new AtomicReference<>(), Optional.empty(), new ArrayList<>(), new ArrayList<>()),
                offerRepository(Optional.of(offer)),
                userRepository(Optional.empty())
        );

        assertThatThrownBy(() -> requestService.findRequestsForOffer(offerId, foreignUserId))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void findMyRequests_returnsRepositoryResult() {
        UUID userId = UUID.randomUUID();
        OfferRequest request = new OfferRequest();
        List<OfferRequest> requestsForUser = List.of(request);

        RequestService requestService = new RequestService(
                offerRequestRepository(new AtomicReference<>(), Optional.empty(), new ArrayList<>(), requestsForUser),
                offerRepository(Optional.empty()),
                userRepository(Optional.empty())
        );

        List<OfferRequest> result = requestService.findMyRequests(userId);

        assertThat(result).containsExactly(request);
    }

    @Test
    void createRequest_throwsNotFoundWhenOfferMissing() {
        UUID offerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        RequestService requestService = new RequestService(
                offerRequestRepository(new AtomicReference<>(), Optional.empty(), new ArrayList<>(), new ArrayList<>()),
                offerRepository(Optional.empty()),
                userRepository(Optional.of(user(requesterId)))
        );

        assertThatThrownBy(() -> requestService.createRequest(offerId, requesterId, "test"))
                .isInstanceOf(NotFoundException.class);
    }

    private Offer offer(UUID offerId, UUID creatorId, OfferStatus status) {
        Offer offer = new Offer();
        offer.setOfferId(offerId);
        offer.setCreator(user(creatorId));
        offer.setStatus(status);
        return offer;
    }

    private User user(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail(userId + "@petsitter.local");
        user.setPasswordHash("hash");
        user.setFirstName("Max");
        user.setLastName("Mustermann");
        user.setStreet("Hauptstrasse");
        user.setHouseNumber("1");
        user.setPostalCode("12345");
        user.setCity("Berlin");
        user.setAccountRole(AccountRole.SIGNED_IN_USER);
        return user;
    }

    private OfferRepository offerRepository(Optional<Offer> offerResult) {
        return (OfferRepository) Proxy.newProxyInstance(
                OfferRepository.class.getClassLoader(),
                new Class<?>[] {OfferRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        UUID requestedId = (UUID) args[0];
                        if (offerResult.isPresent() && offerResult.get().getOfferId().equals(requestedId)) {
                            return offerResult;
                        }
                        return Optional.empty();
                    }
                    if ("toString".equals(method.getName())) {
                        return "OfferRepositoryTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                }
        );
    }

    private UserRepository userRepository(Optional<User> userResult) {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[] {UserRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        UUID requestedId = (UUID) args[0];
                        if (userResult.isPresent() && userResult.get().getId().equals(requestedId)) {
                            return userResult;
                        }
                        return Optional.empty();
                    }
                    if ("toString".equals(method.getName())) {
                        return "UserRepositoryTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                }
        );
    }

    private OfferRequestRepository offerRequestRepository(
            AtomicReference<OfferRequest> savedRequest,
            Optional<OfferRequest> duplicate,
            List<OfferRequest> byIdRequests,
            List<OfferRequest> byRequesterRequests
    ) {
        return (OfferRequestRepository) Proxy.newProxyInstance(
                OfferRequestRepository.class.getClassLoader(),
                new Class<?>[] {OfferRequestRepository.class},
                (proxy, method, args) -> {
                    if ("findByOfferOfferIdAndRequesterId".equals(method.getName())) {
                        return duplicate;
                    }
                    if ("save".equals(method.getName())) {
                        OfferRequest request = (OfferRequest) args[0];
                        savedRequest.set(request);
                        return request;
                    }
                    if ("findById".equals(method.getName())) {
                        UUID requestId = (UUID) args[0];
                        return byIdRequests.stream()
                                .filter(request -> requestId.equals(request.getId()))
                                .findFirst();
                    }
                    if ("findAllByRequesterId".equals(method.getName())) {
                        return byRequesterRequests;
                    }
                    if ("findAllByOfferOfferId".equals(method.getName())) {
                        return byIdRequests;
                    }
                    if ("toString".equals(method.getName())) {
                        return "OfferRequestRepositoryTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                }
        );
    }
}

