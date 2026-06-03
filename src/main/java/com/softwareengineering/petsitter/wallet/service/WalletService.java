package com.softwareengineering.petsitter.wallet.service;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.domain.BookingPause;
import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.booking.repository.BookingPauseRepository;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.notification.service.NotificationService;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.InsufficientBalanceException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.wallet.domain.BookingPayment;
import com.softwareengineering.petsitter.wallet.domain.PaymentStatus;
import com.softwareengineering.petsitter.wallet.domain.RecurringBookingPayment;
import com.softwareengineering.petsitter.wallet.domain.RecurringPaymentStatus;
import com.softwareengineering.petsitter.wallet.domain.WalletAccount;
import com.softwareengineering.petsitter.wallet.domain.WalletTransaction;
import com.softwareengineering.petsitter.wallet.domain.WalletTransactionType;
import com.softwareengineering.petsitter.wallet.dto.BookingPaymentDto;
import com.softwareengineering.petsitter.wallet.dto.RecurringBookingPaymentSummaryDto;
import com.softwareengineering.petsitter.wallet.dto.WalletSummaryDto;
import com.softwareengineering.petsitter.wallet.dto.WalletTransactionDto;
import com.softwareengineering.petsitter.wallet.repository.BookingPaymentRepository;
import com.softwareengineering.petsitter.wallet.repository.RecurringBookingPaymentRepository;
import com.softwareengineering.petsitter.wallet.repository.WalletAccountRepository;
import com.softwareengineering.petsitter.wallet.repository.WalletTransactionRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
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
    private final RecurringBookingPaymentRepository recurringBookingPaymentRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final BookingRepository bookingRepository;
    private final BookingPauseRepository bookingPauseRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    @Autowired
    public WalletService(
            WalletAccountRepository walletAccountRepository,
            BookingPaymentRepository bookingPaymentRepository,
            RecurringBookingPaymentRepository recurringBookingPaymentRepository,
            WalletTransactionRepository walletTransactionRepository,
            BookingRepository bookingRepository,
            BookingPauseRepository bookingPauseRepository,
            NotificationService notificationService
    ) {
        this(walletAccountRepository, bookingPaymentRepository, recurringBookingPaymentRepository,
                walletTransactionRepository, bookingRepository, bookingPauseRepository, notificationService,
                Clock.systemDefaultZone());
    }

    WalletService(
            WalletAccountRepository walletAccountRepository,
            BookingPaymentRepository bookingPaymentRepository,
            WalletTransactionRepository walletTransactionRepository,
            BookingRepository bookingRepository,
            NotificationService notificationService,
            Clock clock
    ) {
        this(walletAccountRepository, bookingPaymentRepository, null, walletTransactionRepository,
                bookingRepository, null, notificationService, clock);
    }

    WalletService(
            WalletAccountRepository walletAccountRepository,
            BookingPaymentRepository bookingPaymentRepository,
            RecurringBookingPaymentRepository recurringBookingPaymentRepository,
            WalletTransactionRepository walletTransactionRepository,
            BookingRepository bookingRepository,
            BookingPauseRepository bookingPauseRepository,
            NotificationService notificationService,
            Clock clock
    ) {
        this.walletAccountRepository = walletAccountRepository;
        this.bookingPaymentRepository = bookingPaymentRepository;
        this.recurringBookingPaymentRepository = recurringBookingPaymentRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.bookingRepository = bookingRepository;
        this.bookingPauseRepository = bookingPauseRepository;
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
        if (recurringBookingPaymentRepository != null) {
            heldOutgoing = heldOutgoing.add(recurringBookingPaymentRepository
                    .findAllByOwnerIdAndStatusIn(userId,
                            List.of(RecurringPaymentStatus.HELD, RecurringPaymentStatus.RELEASE_REQUESTED))
                    .stream()
                    .map(RecurringBookingPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        BigDecimal expectedIncoming = bookingPaymentRepository
                .findAllBySitterIdAndStatusIn(userId, HELD_STATUSES)
                .stream()
                .map(BookingPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (recurringBookingPaymentRepository != null) {
            expectedIncoming = expectedIncoming.add(recurringBookingPaymentRepository
                    .findAllBySitterIdAndStatusIn(userId,
                            List.of(RecurringPaymentStatus.HELD, RecurringPaymentStatus.RELEASE_REQUESTED))
                    .stream()
                    .map(RecurringBookingPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
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
        saveTransaction(account, (BookingPayment) null, WalletTransactionType.DEV_TOP_UP, normalized,
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
    public void fundCurrentRecurringWeek(Booking booking) {
        requireRecurringInfrastructure();
        if (!isRecurringBooking(booking)) {
            return;
        }
        LocalDate today = LocalDate.now(clock);
        LocalDate periodStart = weekStart(today);
        LocalDate periodEnd = periodStart.plusDays(6);
        fundRecurringPeriod(booking, periodStart, periodEnd, today);
    }

    @Transactional
    @Scheduled(cron = "${petsitter.wallet.recurring-funding-cron:0 0 3 * * MON}")
    public int fundWeeklyRecurringBookings() {
        requireRecurringInfrastructure();
        LocalDate today = LocalDate.now(clock);
        LocalDate periodStart = weekStart(today);
        LocalDate periodEnd = periodStart.plusDays(6);
        int processed = 0;
        for (Booking booking : bookingRepository.findAllActiveRecurring(
                BookingStatus.CREATED,
                OfferFrequency.REGULAR)) {
            fundRecurringPeriod(booking, periodStart, periodEnd, periodStart);
            processed++;
        }
        return processed;
    }

    @Transactional
    public void recalculateHeldRecurringPaymentsForPause(Booking booking, LocalDate pauseStart, LocalDate pauseEnd) {
        requireRecurringInfrastructure();
        if (!isRecurringBooking(booking) || pauseStart == null || pauseEnd == null) {
            return;
        }
        List<RecurringBookingPayment> payments = recurringBookingPaymentRepository
                .findAllByBookingIdAndStatusInOrderByPeriodStartAsc(
                        booking.getId(),
                        List.of(RecurringPaymentStatus.HELD, RecurringPaymentStatus.RELEASE_REQUESTED))
                .stream()
                .filter(payment -> !payment.getPeriodEnd().isBefore(pauseStart)
                        && !payment.getPeriodStart().isAfter(pauseEnd))
                .toList();
        for (RecurringBookingPayment payment : payments) {
            adjustHeldRecurringPayment(payment);
        }
    }

    @Transactional(readOnly = true)
    public RecurringBookingPaymentSummaryDto getRecurringPaymentSummary(UUID bookingId) {
        if (recurringBookingPaymentRepository == null || bookingId == null) {
            return RecurringBookingPaymentSummaryDto.empty();
        }
        LocalDate today = LocalDate.now(clock);
        List<RecurringBookingPayment> payable = recurringBookingPaymentRepository
                .findAllByBookingIdAndStatusInOrderByPeriodStartAsc(
                        bookingId,
                        List.of(RecurringPaymentStatus.HELD, RecurringPaymentStatus.RELEASE_REQUESTED))
                .stream()
                .filter(payment -> payment.getPeriodEnd().isBefore(today))
                .toList();
        if (payable.isEmpty()) {
            return RecurringBookingPaymentSummaryDto.empty();
        }
        int occurrences = payable.stream()
                .mapToInt(RecurringBookingPayment::getOccurrenceCount)
                .sum();
        BigDecimal amount = payable.stream()
                .map(RecurringBookingPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        RecurringPaymentStatus status = payable.stream()
                .anyMatch(payment -> payment.getStatus() == RecurringPaymentStatus.RELEASE_REQUESTED)
                ? RecurringPaymentStatus.RELEASE_REQUESTED
                : RecurringPaymentStatus.HELD;
        LocalDateTime requestedAt = payable.stream()
                .map(RecurringBookingPayment::getReleaseRequestedAt)
                .filter(java.util.Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        return new RecurringBookingPaymentSummaryDto(
                status,
                occurrences,
                amount,
                requestedAt,
                requestedAt == null ? null : requestedAt.plusDays(7));
    }

    @Transactional
    public void releaseRecurringPayments(UUID bookingId, UUID currentUserId) {
        requireRecurringInfrastructure();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking nicht gefunden: " + bookingId));
        if (!booking.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenOperationException("Nur der Tierhalter darf die Auszahlung freigeben.");
        }
        payoutRecurringPayments(payableRecurringPayments(bookingId));
    }

    @Transactional
    public void requestRecurringRelease(UUID bookingId, UUID currentUserId) {
        requireRecurringInfrastructure();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking nicht gefunden: " + bookingId));
        if (!booking.getSitter().getId().equals(currentUserId)) {
            throw new ForbiddenOperationException("Nur der Tiersitter darf die Auszahlung anfordern.");
        }
        List<RecurringBookingPayment> payable = payableRecurringPayments(bookingId).stream()
                .filter(payment -> payment.getStatus() == RecurringPaymentStatus.HELD)
                .toList();
        if (payable.isEmpty()) {
            throw new BusinessRuleViolationException("Es gibt keine abgeschlossenen regelmaessigen Termine zur Auszahlung.");
        }
        LocalDateTime requestedAt = now();
        for (RecurringBookingPayment payment : payable) {
            payment.setStatus(RecurringPaymentStatus.RELEASE_REQUESTED);
            payment.setReleaseRequestedAt(requestedAt);
            recurringBookingPaymentRepository.save(payment);
        }
        notificationService.createPayoutRequestedNotification(booking.getOwner(), booking);
    }

    @Transactional
    @Scheduled(
            initialDelayString = "${petsitter.wallet.recurring-payout-check-initial-delay-ms:3600000}",
            fixedDelayString = "${petsitter.wallet.recurring-payout-check-interval-ms:3600000}")
    public int releaseExpiredRecurringRequests() {
        if (recurringBookingPaymentRepository == null) {
            return 0;
        }
        LocalDateTime threshold = now().minusDays(7);
        List<RecurringBookingPayment> expired = recurringBookingPaymentRepository
                .findAllByStatusAndReleaseRequestedAtLessThanEqual(
                        RecurringPaymentStatus.RELEASE_REQUESTED,
                        threshold);
        int released = 0;
        for (RecurringBookingPayment payment : expired) {
            RecurringBookingPayment locked = recurringBookingPaymentRepository
                    .findByBookingIdAndPeriodStartForUpdate(
                            payment.getBooking().getId(),
                            payment.getPeriodStart())
                    .orElse(null);
            if (locked != null
                    && locked.getStatus() == RecurringPaymentStatus.RELEASE_REQUESTED
                    && locked.getReleaseRequestedAt() != null
                    && !locked.getReleaseRequestedAt().isAfter(threshold)) {
                payoutRecurringPayments(List.of(locked));
                released++;
            }
        }
        return released;
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

    private void fundRecurringPeriod(
            Booking booking,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate effectiveFrom
    ) {
        RecurringBookingPayment existing = recurringBookingPaymentRepository
                .findByBookingIdAndPeriodStartForUpdate(booking.getId(), periodStart)
                .orElse(null);
        if (existing != null
                && existing.getStatus() != RecurringPaymentStatus.AWAITING_FUNDS
                && existing.getStatus() != RecurringPaymentStatus.SKIPPED) {
            return;
        }

        int occurrenceCount = countBillableOccurrences(booking, periodStart, periodEnd, effectiveFrom);
        BigDecimal price = normalizeMoney(booking.getPricePerDay());
        BigDecimal amount = normalizeMoney(price.multiply(BigDecimal.valueOf(occurrenceCount)));
        RecurringBookingPayment payment = existing != null
                ? existing
                : newRecurringPayment(booking, periodStart, periodEnd, price);
        payment.setOccurrenceCount(occurrenceCount);
        payment.setAmount(amount);

        if (occurrenceCount == 0) {
            payment.setStatus(RecurringPaymentStatus.SKIPPED);
            recurringBookingPaymentRepository.save(payment);
            return;
        }

        WalletAccount ownerAccount = lockedAccount(booking.getOwner().getId());
        if (ownerAccount.getAvailableBalance().compareTo(amount) < 0) {
            payment.setStatus(RecurringPaymentStatus.AWAITING_FUNDS);
            recurringBookingPaymentRepository.save(payment);
            notificationService.createWalletTopUpRequiredNotification(booking.getOwner().getId());
            return;
        }

        ownerAccount.setAvailableBalance(ownerAccount.getAvailableBalance().subtract(amount));
        walletAccountRepository.save(ownerAccount);
        payment.setStatus(RecurringPaymentStatus.HELD);
        payment.setHeldAt(now());
        payment = recurringBookingPaymentRepository.save(payment);
        saveTransaction(ownerAccount, payment, WalletTransactionType.ESCROW_HOLD,
                amount.negate(), "Woechentliche Treuhandzahlung fuer " + bookingTitle(booking));
    }

    private RecurringBookingPayment newRecurringPayment(
            Booking booking,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal price
    ) {
        RecurringBookingPayment payment = new RecurringBookingPayment();
        payment.setBooking(booking);
        payment.setOwner(booking.getOwner());
        payment.setSitter(booking.getSitter());
        payment.setPeriodStart(periodStart);
        payment.setPeriodEnd(periodEnd);
        payment.setPricePerOccurrence(price);
        return payment;
    }

    private List<RecurringBookingPayment> payableRecurringPayments(UUID bookingId) {
        LocalDate today = LocalDate.now(clock);
        List<RecurringBookingPayment> payable = recurringBookingPaymentRepository
                .findAllByBookingIdAndStatusInOrderByPeriodStartAsc(
                        bookingId,
                        List.of(RecurringPaymentStatus.HELD, RecurringPaymentStatus.RELEASE_REQUESTED))
                .stream()
                .filter(payment -> payment.getPeriodEnd().isBefore(today))
                .toList();
        if (payable.isEmpty()) {
            throw new BusinessRuleViolationException("Es gibt keine abgeschlossenen regelmaessigen Termine zur Auszahlung.");
        }
        return payable;
    }

    private void payoutRecurringPayments(List<RecurringBookingPayment> payments) {
        if (payments == null || payments.isEmpty()) {
            throw new BusinessRuleViolationException("Es gibt keine abgeschlossenen regelmaessigen Termine zur Auszahlung.");
        }
        for (RecurringBookingPayment payment : payments) {
            if (payment.getStatus() != RecurringPaymentStatus.HELD
                    && payment.getStatus() != RecurringPaymentStatus.RELEASE_REQUESTED) {
                throw new BusinessRuleViolationException("Die Zahlung kann in diesem Status nicht ausgezahlt werden.");
            }
        }

        RecurringBookingPayment first = payments.getFirst();
        WalletAccount sitterAccount = lockedAccount(first.getSitter().getId());
        BigDecimal totalAmount = payments.stream()
                .map(RecurringBookingPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        sitterAccount.setAvailableBalance(sitterAccount.getAvailableBalance().add(totalAmount));
        walletAccountRepository.save(sitterAccount);

        for (RecurringBookingPayment payment : payments) {
            payment.setStatus(RecurringPaymentStatus.RELEASED);
            payment.setReleasedAt(now());
            recurringBookingPaymentRepository.save(payment);
            saveTransaction(sitterAccount, payment, WalletTransactionType.PAYOUT,
                    payment.getAmount(), "Auszahlung regelmaessiger Termine fuer " + bookingTitle(payment.getBooking()));
        }
        notificationService.createPayoutReleasedNotification(first.getSitter(), first.getBooking());
    }

    private void adjustHeldRecurringPayment(RecurringBookingPayment payment) {
        int updatedCount = countBillableOccurrences(
                payment.getBooking(),
                payment.getPeriodStart(),
                payment.getPeriodEnd(),
                payment.getPeriodStart());
        BigDecimal updatedAmount = normalizeMoney(
                payment.getPricePerOccurrence().multiply(BigDecimal.valueOf(updatedCount)));
        BigDecimal refund = payment.getAmount().subtract(updatedAmount);
        if (refund.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        WalletAccount ownerAccount = lockedAccount(payment.getOwner().getId());
        ownerAccount.setAvailableBalance(ownerAccount.getAvailableBalance().add(refund));
        walletAccountRepository.save(ownerAccount);

        payment.setOccurrenceCount(updatedCount);
        payment.setAmount(updatedAmount);
        if (updatedCount == 0) {
            payment.setStatus(RecurringPaymentStatus.REFUNDED);
            payment.setRefundedAt(now());
        }
        recurringBookingPaymentRepository.save(payment);
        saveTransaction(ownerAccount, payment, WalletTransactionType.REFUND,
                refund, "Erstattung pausierter Termine fuer " + bookingTitle(payment.getBooking()));
    }

    private int countBillableOccurrences(
            Booking booking,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate effectiveFrom
    ) {
        if (!isRecurringBooking(booking) || booking.getPricePerDay() == null) {
            return 0;
        }
        LocalDate from = maxDate(periodStart, effectiveFrom);
        LocalDate end = booking.getRecurringEndedOn() == null
                ? periodEnd
                : minDate(periodEnd, booking.getRecurringEndedOn());
        if (from == null || end == null || end.isBefore(from)) {
            return 0;
        }
        Set<DayOfWeek> weekdays = booking.getRecurringWeekdays();
        if (weekdays.isEmpty()) {
            return 0;
        }
        List<BookingPause> pauses = bookingPauseRepository == null
                ? List.of()
                : bookingPauseRepository.findAllOverlapping(booking.getId(), periodStart, periodEnd);

        int count = 0;
        LocalDate cursor = from;
        while (!cursor.isAfter(end)) {
            if (weekdays.contains(cursor.getDayOfWeek()) && !isPaused(cursor, pauses)) {
                count++;
            }
            cursor = cursor.plusDays(1);
        }
        return count;
    }

    private boolean isPaused(LocalDate date, Collection<BookingPause> pauses) {
        return pauses != null && pauses.stream()
                .anyMatch(pause -> !date.isBefore(pause.getStartDate()) && !date.isAfter(pause.getEndDate()));
    }

    private boolean isRecurringBooking(Booking booking) {
        return booking != null
                && booking.getFrequency() == OfferFrequency.REGULAR
                && booking.getStatus() == BookingStatus.CREATED;
    }

    private LocalDate weekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private LocalDate maxDate(LocalDate first, LocalDate second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.isAfter(second) ? first : second;
    }

    private LocalDate minDate(LocalDate first, LocalDate second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.isBefore(second) ? first : second;
    }

    private void requireRecurringInfrastructure() {
        if (recurringBookingPaymentRepository == null || bookingPauseRepository == null) {
            throw new BusinessRuleViolationException("Regelmaessige Zahlungen sind nicht konfiguriert.");
        }
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
        saveTransaction(account, payment, null, type, amount, description);
    }

    private void saveTransaction(
            WalletAccount account,
            RecurringBookingPayment payment,
            WalletTransactionType type,
            BigDecimal amount,
            String description
    ) {
        saveTransaction(account, null, payment, type, amount, description);
    }

    private void saveTransaction(
            WalletAccount account,
            BookingPayment payment,
            RecurringBookingPayment recurringPayment,
            WalletTransactionType type,
            BigDecimal amount,
            String description
    ) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletAccount(account);
        transaction.setBookingPayment(payment);
        transaction.setRecurringBookingPayment(recurringPayment);
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
