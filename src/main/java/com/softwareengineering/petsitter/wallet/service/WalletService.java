package com.softwareengineering.petsitter.wallet.service;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.notification.service.NotificationService;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.InsufficientBalanceException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.wallet.domain.BookingPayment;
import com.softwareengineering.petsitter.wallet.domain.PaymentStatus;
import com.softwareengineering.petsitter.wallet.domain.WalletAccount;
import com.softwareengineering.petsitter.wallet.domain.WalletTransaction;
import com.softwareengineering.petsitter.wallet.domain.WalletTransactionType;
import com.softwareengineering.petsitter.wallet.dto.BookingPaymentDto;
import com.softwareengineering.petsitter.wallet.dto.WalletSummaryDto;
import com.softwareengineering.petsitter.wallet.dto.WalletTransactionDto;
import com.softwareengineering.petsitter.wallet.repository.BookingPaymentRepository;
import com.softwareengineering.petsitter.wallet.repository.WalletAccountRepository;
import com.softwareengineering.petsitter.wallet.repository.WalletTransactionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private static final Set<BigDecimal> DEV_TOP_UP_AMOUNTS = Set.of(
            new BigDecimal("25.00"),
            new BigDecimal("50.00"),
            new BigDecimal("100.00"));
    private static final List<PaymentStatus> HELD_STATUSES = List.of(
            PaymentStatus.HELD,
            PaymentStatus.RELEASE_REQUESTED);

    private final WalletAccountRepository walletAccountRepository;
    private final BookingPaymentRepository bookingPaymentRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    @Autowired
    public WalletService(
            WalletAccountRepository walletAccountRepository,
            BookingPaymentRepository bookingPaymentRepository,
            WalletTransactionRepository walletTransactionRepository,
            BookingRepository bookingRepository,
            NotificationService notificationService
    ) {
        this(walletAccountRepository, bookingPaymentRepository, walletTransactionRepository,
                bookingRepository, notificationService, Clock.systemDefaultZone());
    }

    WalletService(
            WalletAccountRepository walletAccountRepository,
            BookingPaymentRepository bookingPaymentRepository,
            WalletTransactionRepository walletTransactionRepository,
            BookingRepository bookingRepository,
            NotificationService notificationService,
            Clock clock
    ) {
        this.walletAccountRepository = walletAccountRepository;
        this.bookingPaymentRepository = bookingPaymentRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @Transactional
    public WalletAccount createWalletIfMissing(User user) {
        if (user == null || user.getId() == null) {
            throw new BusinessRuleViolationException("Wallet kann nur fuer einen gespeicherten User angelegt werden.");
        }
        return walletAccountRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    WalletAccount account = new WalletAccount();
                    account.setUser(user);
                    account.setAvailableBalance(BigDecimal.ZERO);
                    return walletAccountRepository.save(account);
                });
    }

    @Transactional(readOnly = true)
    public BigDecimal getAvailableBalance(UUID userId) {
        return walletAccountRepository.findByUserId(userId)
                .map(WalletAccount::getAvailableBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public WalletSummaryDto getWalletSummary(UUID userId) {
        WalletAccount account = walletAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Wallet nicht gefunden: " + userId));

        BigDecimal heldOutgoing = bookingPaymentRepository
                .findAllByOwnerIdAndStatusIn(userId, HELD_STATUSES)
                .stream()
                .map(BookingPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expectedIncoming = bookingPaymentRepository
                .findAllBySitterIdAndStatusIn(userId, HELD_STATUSES)
                .stream()
                .map(BookingPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<WalletTransactionDto> transactions = walletTransactionRepository
                .findTop50ByWalletAccountUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(transaction -> new WalletTransactionDto(
                        transaction.getType(),
                        transaction.getAmount(),
                        transaction.getBalanceAfter(),
                        transaction.getDescription(),
                        transaction.getCreatedAt()))
                .toList();

        return new WalletSummaryDto(
                account.getAvailableBalance(),
                heldOutgoing,
                expectedIncoming,
                transactions);
    }

    @Transactional
    public void devTopUp(UUID userId, BigDecimal amount) {
        BigDecimal normalized = normalizeMoney(amount);
        if (!DEV_TOP_UP_AMOUNTS.contains(normalized)) {
            throw new BusinessRuleViolationException("Erlaubte Demo-Aufladungen sind 25, 50 oder 100 EUR.");
        }

        WalletAccount account = lockedAccount(userId);
        account.setAvailableBalance(account.getAvailableBalance().add(normalized));
        walletAccountRepository.save(account);
        saveTransaction(account, null, WalletTransactionType.DEV_TOP_UP, normalized,
                "Demo-Guthaben aufgeladen");
    }

    @Transactional
    public BookingPayment holdForBooking(Booking booking) {
        if (booking == null || booking.getId() == null || booking.getOwner() == null
                || booking.getSitter() == null || booking.getTotalPrice() == null) {
            throw new BusinessRuleViolationException("Booking-Daten fuer die Treuhandzahlung sind unvollstaendig.");
        }
        if (bookingPaymentRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new BusinessRuleViolationException("Fuer diese Buchung existiert bereits eine Zahlung.");
        }

        BigDecimal totalPrice = normalizeMoney(booking.getTotalPrice());
        WalletAccount ownerAccount = lockedAccount(booking.getOwner().getId());
        if (ownerAccount.getAvailableBalance().compareTo(totalPrice) < 0) {
            throw new InsufficientBalanceException(
                    booking.getOwner().getId(),
                    totalPrice.subtract(ownerAccount.getAvailableBalance()));
        }

        ownerAccount.setAvailableBalance(ownerAccount.getAvailableBalance().subtract(totalPrice));
        walletAccountRepository.save(ownerAccount);

        BookingPayment payment = new BookingPayment();
        payment.setBooking(booking);
        payment.setOwner(booking.getOwner());
        payment.setSitter(booking.getSitter());
        payment.setAmount(totalPrice);
        payment.setStatus(PaymentStatus.HELD);
        payment.setHeldAt(now());
        payment = bookingPaymentRepository.save(payment);

        saveTransaction(ownerAccount, payment, WalletTransactionType.ESCROW_HOLD,
                totalPrice.negate(), "Treuhandzahlung fuer " + bookingTitle(booking));
        return payment;
    }

    @Transactional
    public void refundForCancelledBooking(Booking booking) {
        if (booking == null || booking.getId() == null) {
            return;
        }
        BookingPayment payment = bookingPaymentRepository.findByBookingIdForUpdate(booking.getId())
                .orElse(null);
        if (payment == null || payment.getStatus() == PaymentStatus.LEGACY_UNFUNDED
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            return;
        }
        if (payment.getStatus() == PaymentStatus.RELEASED) {
            throw new BusinessRuleViolationException("Eine bereits ausgezahlte Buchung kann nicht storniert werden.");
        }

        WalletAccount ownerAccount = lockedAccount(payment.getOwner().getId());
        ownerAccount.setAvailableBalance(ownerAccount.getAvailableBalance().add(payment.getAmount()));
        walletAccountRepository.save(ownerAccount);

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(now());
        bookingPaymentRepository.save(payment);
        saveTransaction(ownerAccount, payment, WalletTransactionType.REFUND,
                payment.getAmount(), "Erstattung fuer " + bookingTitle(booking));
    }

    @Transactional
    public void releasePayment(UUID bookingId, UUID currentUserId) {
        BookingPayment payment = lockedPayment(bookingId);
        if (!payment.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenOperationException("Nur der Tierhalter darf die Auszahlung freigeben.");
        }
        requireServiceFinished(payment.getBooking());
        payout(payment);
    }

    @Transactional
    public void requestRelease(UUID bookingId, UUID currentUserId) {
        BookingPayment payment = lockedPayment(bookingId);
        if (!payment.getSitter().getId().equals(currentUserId)) {
            throw new ForbiddenOperationException("Nur der Tiersitter darf die Auszahlung anfordern.");
        }
        requireServiceFinished(payment.getBooking());
        if (payment.getStatus() != PaymentStatus.HELD) {
            throw new BusinessRuleViolationException("Die Auszahlung kann in diesem Status nicht angefordert werden.");
        }

        payment.setStatus(PaymentStatus.RELEASE_REQUESTED);
        payment.setReleaseRequestedAt(now());
        bookingPaymentRepository.save(payment);
        notificationService.createPayoutRequestedNotification(payment.getOwner(), payment.getBooking());
    }

    @Transactional
    @Scheduled(
            initialDelayString = "${petsitter.wallet.payout-check-initial-delay-ms:3600000}",
            fixedDelayString = "${petsitter.wallet.payout-check-interval-ms:3600000}")
    public int releaseExpiredRequests() {
        LocalDateTime threshold = now().minusDays(7);
        List<BookingPayment> expired = bookingPaymentRepository
                .findAllByStatusAndReleaseRequestedAtLessThanEqual(PaymentStatus.RELEASE_REQUESTED, threshold);
        int released = 0;
        for (BookingPayment candidate : expired) {
            BookingPayment payment = bookingPaymentRepository.findByBookingIdForUpdate(
                    candidate.getBooking().getId()).orElse(null);
            if (payment != null
                    && payment.getStatus() == PaymentStatus.RELEASE_REQUESTED
                    && payment.getReleaseRequestedAt() != null
                    && !payment.getReleaseRequestedAt().isAfter(threshold)) {
                payout(payment);
                released++;
            }
        }
        return released;
    }

    @Transactional(readOnly = true)
    public BookingPaymentDto getPaymentForBooking(UUID bookingId) {
        return bookingPaymentRepository.findByBookingId(bookingId)
                .map(this::toDto)
                .orElse(null);
    }

    public void notifyTopUpRequired(UUID ownerId) {
        notificationService.createWalletTopUpRequiredNotification(ownerId);
    }

    private void payout(BookingPayment payment) {
        if (payment.getStatus() != PaymentStatus.HELD
                && payment.getStatus() != PaymentStatus.RELEASE_REQUESTED) {
            throw new BusinessRuleViolationException("Die Zahlung kann in diesem Status nicht ausgezahlt werden.");
        }

        WalletAccount sitterAccount = lockedAccount(payment.getSitter().getId());
        sitterAccount.setAvailableBalance(sitterAccount.getAvailableBalance().add(payment.getAmount()));
        walletAccountRepository.save(sitterAccount);

        payment.setStatus(PaymentStatus.RELEASED);
        payment.setReleasedAt(now());
        bookingPaymentRepository.save(payment);
        saveTransaction(sitterAccount, payment, WalletTransactionType.PAYOUT,
                payment.getAmount(), "Auszahlung fuer " + bookingTitle(payment.getBooking()));

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
        notificationService.createPayoutReleasedNotification(payment.getSitter(), booking);
    }

    private WalletAccount lockedAccount(UUID userId) {
        return walletAccountRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("Wallet nicht gefunden: " + userId));
    }

    private BookingPayment lockedPayment(UUID bookingId) {
        return bookingPaymentRepository.findByBookingIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Zahlung fuer Booking nicht gefunden: " + bookingId));
    }

    private void requireServiceFinished(Booking booking) {
        if (booking.getEndDate() == null || !LocalDate.now(clock).isAfter(booking.getEndDate())) {
            throw new BusinessRuleViolationException("Die Auszahlung ist erst nach dem Enddatum moeglich.");
        }
    }

    private void saveTransaction(
            WalletAccount account,
            BookingPayment payment,
            WalletTransactionType type,
            BigDecimal amount,
            String description
    ) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletAccount(account);
        transaction.setBookingPayment(payment);
        transaction.setType(type);
        transaction.setAmount(normalizeMoney(amount));
        transaction.setBalanceAfter(normalizeMoney(account.getAvailableBalance()));
        transaction.setDescription(description);
        walletTransactionRepository.save(transaction);
    }

    private BookingPaymentDto toDto(BookingPayment payment) {
        LocalDateTime automaticReleaseAt = payment.getReleaseRequestedAt() == null
                ? null
                : payment.getReleaseRequestedAt().plusDays(7);
        return new BookingPaymentDto(
                payment.getAmount(),
                payment.getStatus(),
                payment.getReleaseRequestedAt(),
                automaticReleaseAt);
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessRuleViolationException("Betrag darf nicht leer sein.");
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String bookingTitle(Booking booking) {
        return booking.getOffer() != null
                && booking.getOffer().getTitle() != null
                && !booking.getOffer().getTitle().isBlank()
                ? booking.getOffer().getTitle()
                : "Betreuungsvereinbarung";
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
