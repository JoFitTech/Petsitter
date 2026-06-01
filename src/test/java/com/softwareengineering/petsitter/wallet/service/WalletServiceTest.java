package com.softwareengineering.petsitter.wallet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.notification.service.NotificationService;
import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.InsufficientBalanceException;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.wallet.domain.BookingPayment;
import com.softwareengineering.petsitter.wallet.domain.PaymentStatus;
import com.softwareengineering.petsitter.wallet.domain.WalletAccount;
import com.softwareengineering.petsitter.wallet.domain.WalletTransaction;
import com.softwareengineering.petsitter.wallet.domain.WalletTransactionType;
import com.softwareengineering.petsitter.wallet.repository.BookingPaymentRepository;
import com.softwareengineering.petsitter.wallet.repository.WalletAccountRepository;
import com.softwareengineering.petsitter.wallet.repository.WalletTransactionRepository;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class WalletServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void devTopUpCreditsWalletAndCreatesLedgerEntry() {
        Scenario scenario = scenario("10.00", "0.00", "30.00", LocalDate.of(2026, 6, 3));

        scenario.service.devTopUp(scenario.owner.getId(), new BigDecimal("25.00"));

        assertThat(scenario.ownerAccount.getAvailableBalance()).isEqualByComparingTo("35.00");
        assertThat(scenario.transactions).singleElement().satisfies(transaction -> {
            assertThat(transaction.getType()).isEqualTo(WalletTransactionType.DEV_TOP_UP);
            assertThat(transaction.getAmount()).isEqualByComparingTo("25.00");
            assertThat(transaction.getBalanceAfter()).isEqualByComparingTo("35.00");
        });
    }

    @Test
    void holdForBookingDebitsOwnerAndCreatesEscrowTransaction() {
        Scenario scenario = scenario("100.00", "0.00", "30.00", LocalDate.of(2026, 6, 3));

        BookingPayment payment = scenario.service.holdForBooking(scenario.booking);

        assertThat(scenario.ownerAccount.getAvailableBalance()).isEqualByComparingTo("70.00");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.HELD);
        assertThat(payment.getAmount()).isEqualByComparingTo("30.00");
        assertThat(scenario.transactions).singleElement().satisfies(transaction -> {
            assertThat(transaction.getType()).isEqualTo(WalletTransactionType.ESCROW_HOLD);
            assertThat(transaction.getAmount()).isEqualByComparingTo("-30.00");
            assertThat(transaction.getBalanceAfter()).isEqualByComparingTo("70.00");
        });
    }

    @Test
    void holdForBookingRejectsInsufficientBalanceWithoutMutation() {
        Scenario scenario = scenario("10.00", "0.00", "30.00", LocalDate.of(2026, 6, 3));

        assertThatThrownBy(() -> scenario.service.holdForBooking(scenario.booking))
                .isInstanceOf(InsufficientBalanceException.class)
                .satisfies(exception -> assertThat(((InsufficientBalanceException) exception).getMissingAmount())
                        .isEqualByComparingTo("20.00"));

        assertThat(scenario.ownerAccount.getAvailableBalance()).isEqualByComparingTo("10.00");
        assertThat(scenario.payment.get()).isNull();
        assertThat(scenario.transactions).isEmpty();
    }

    @Test
    void holdForBookingRejectsDuplicateReservation() {
        Scenario scenario = scenario("100.00", "0.00", "30.00", LocalDate.of(2026, 6, 3));
        scenario.existingPayment(PaymentStatus.HELD);

        assertThatThrownBy(() -> scenario.service.holdForBooking(scenario.booking))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(scenario.ownerAccount.getAvailableBalance()).isEqualByComparingTo("100.00");
        assertThat(scenario.transactions).isEmpty();
    }

    @Test
    void refundForCancelledBookingReturnsHeldAmountToOwner() {
        Scenario scenario = scenario("70.00", "0.00", "30.00", LocalDate.of(2026, 6, 3));
        BookingPayment payment = scenario.existingPayment(PaymentStatus.HELD);

        scenario.service.refundForCancelledBooking(scenario.booking);

        assertThat(scenario.ownerAccount.getAvailableBalance()).isEqualByComparingTo("100.00");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(scenario.transactions).singleElement()
                .extracting(WalletTransaction::getType)
                .isEqualTo(WalletTransactionType.REFUND);
    }

    @Test
    void releasePaymentCreditsSitterAndCompletesBooking() {
        Scenario scenario = scenario("70.00", "0.00", "30.00", LocalDate.of(2026, 5, 31));
        BookingPayment payment = scenario.existingPayment(PaymentStatus.HELD);

        scenario.service.releasePayment(scenario.booking.getId(), scenario.owner.getId());

        assertThat(scenario.sitterAccount.getAvailableBalance()).isEqualByComparingTo("30.00");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.RELEASED);
        assertThat(scenario.booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(scenario.transactions).singleElement()
                .extracting(WalletTransaction::getType)
                .isEqualTo(WalletTransactionType.PAYOUT);
    }

    @Test
    void releasePaymentRejectsReleaseBeforeEndDate() {
        Scenario scenario = scenario("70.00", "0.00", "30.00", LocalDate.of(2026, 6, 2));
        scenario.existingPayment(PaymentStatus.HELD);

        assertThatThrownBy(() -> scenario.service.releasePayment(
                scenario.booking.getId(), scenario.owner.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(scenario.sitterAccount.getAvailableBalance()).isEqualByComparingTo("0.00");
        assertThat(scenario.booking.getStatus()).isEqualTo(BookingStatus.CREATED);
    }

    @Test
    void releasePaymentRejectsReleaseOnEndDate() {
        Scenario scenario = scenario("70.00", "0.00", "30.00", LocalDate.of(2026, 6, 1));
        scenario.existingPayment(PaymentStatus.HELD);

        assertThatThrownBy(() -> scenario.service.releasePayment(
                scenario.booking.getId(), scenario.owner.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(scenario.sitterAccount.getAvailableBalance()).isEqualByComparingTo("0.00");
        assertThat(scenario.booking.getStatus()).isEqualTo(BookingStatus.CREATED);
    }

    @Test
    void requestReleaseStoresRequestForFinishedBooking() {
        Scenario scenario = scenario("70.00", "0.00", "30.00", LocalDate.of(2026, 5, 31));
        BookingPayment payment = scenario.existingPayment(PaymentStatus.HELD);

        scenario.service.requestRelease(scenario.booking.getId(), scenario.sitter.getId());

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.RELEASE_REQUESTED);
        assertThat(payment.getReleaseRequestedAt()).isEqualTo(LocalDateTime.of(2026, 6, 1, 12, 0));
        assertThat(scenario.sitterAccount.getAvailableBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void releaseExpiredRequestsPaysOutSevenDaysAfterSitterRequest() {
        Scenario scenario = scenario("70.00", "0.00", "30.00", LocalDate.of(2026, 5, 20));
        BookingPayment payment = scenario.existingPayment(PaymentStatus.RELEASE_REQUESTED);
        payment.setReleaseRequestedAt(LocalDateTime.of(2026, 5, 24, 11, 59));

        int released = scenario.service.releaseExpiredRequests();

        assertThat(released).isEqualTo(1);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.RELEASED);
        assertThat(scenario.sitterAccount.getAvailableBalance()).isEqualByComparingTo("30.00");
    }

    private Scenario scenario(
            String ownerBalance,
            String sitterBalance,
            String totalPrice,
            LocalDate endDate
    ) {
        User owner = user();
        User sitter = user();
        WalletAccount ownerAccount = account(owner, ownerBalance);
        WalletAccount sitterAccount = account(sitter, sitterBalance);
        Booking booking = booking(owner, sitter, totalPrice, endDate);
        AtomicReference<BookingPayment> payment = new AtomicReference<>();
        List<WalletTransaction> transactions = new ArrayList<>();
        Map<UUID, WalletAccount> accounts = new HashMap<>();
        accounts.put(owner.getId(), ownerAccount);
        accounts.put(sitter.getId(), sitterAccount);

        WalletService service = new WalletService(
                walletRepository(accounts),
                paymentRepository(payment),
                transactionRepository(transactions),
                bookingRepository(),
                notificationService(),
                CLOCK);
        return new Scenario(service, owner, sitter, ownerAccount, sitterAccount, booking, payment, transactions);
    }

    private WalletAccountRepository walletRepository(Map<UUID, WalletAccount> accounts) {
        return proxy(WalletAccountRepository.class, (method, args) -> switch (method) {
            case "findByUserId", "findByUserIdForUpdate" -> Optional.ofNullable(accounts.get(args[0]));
            case "save" -> args[0];
            case "toString" -> "WalletAccountRepositoryTestDouble";
            default -> throw new UnsupportedOperationException("Not stubbed: " + method);
        });
    }

    private BookingPaymentRepository paymentRepository(AtomicReference<BookingPayment> payment) {
        return proxy(BookingPaymentRepository.class, (method, args) -> switch (method) {
            case "findByBookingId", "findByBookingIdForUpdate" -> Optional.ofNullable(payment.get());
            case "findAllByStatusAndReleaseRequestedAtLessThanEqual" -> payment.get() == null
                    ? List.of()
                    : List.of(payment.get());
            case "save" -> {
                BookingPayment saved = (BookingPayment) args[0];
                if (saved.getId() == null) saved.setId(UUID.randomUUID());
                payment.set(saved);
                yield saved;
            }
            case "toString" -> "BookingPaymentRepositoryTestDouble";
            default -> throw new UnsupportedOperationException("Not stubbed: " + method);
        });
    }

    private WalletTransactionRepository transactionRepository(List<WalletTransaction> transactions) {
        return proxy(WalletTransactionRepository.class, (method, args) -> switch (method) {
            case "save" -> {
                WalletTransaction transaction = (WalletTransaction) args[0];
                transactions.add(transaction);
                yield transaction;
            }
            case "toString" -> "WalletTransactionRepositoryTestDouble";
            default -> throw new UnsupportedOperationException("Not stubbed: " + method);
        });
    }

    private BookingRepository bookingRepository() {
        return proxy(BookingRepository.class, (method, args) -> switch (method) {
            case "save" -> args[0];
            case "toString" -> "BookingRepositoryTestDouble";
            default -> throw new UnsupportedOperationException("Not stubbed: " + method);
        });
    }

    private NotificationService notificationService() {
        return new NotificationService(null, null, null) {
            @Override
            public void createPayoutRequestedNotification(User owner, Booking booking) {
            }

            @Override
            public void createPayoutReleasedNotification(User sitter, Booking booking) {
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> invocation.invoke(method.getName(), args == null ? new Object[0] : args));
    }

    private User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }

    private WalletAccount account(User user, String balance) {
        WalletAccount account = new WalletAccount();
        account.setId(UUID.randomUUID());
        account.setUser(user);
        account.setAvailableBalance(new BigDecimal(balance));
        return account;
    }

    private Booking booking(User owner, User sitter, String totalPrice, LocalDate endDate) {
        Offer offer = new Offer();
        offer.setTitle("Urlaubsbetreuung");
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setOffer(offer);
        booking.setOwner(owner);
        booking.setSitter(sitter);
        booking.setStartDate(endDate.minusDays(2));
        booking.setEndDate(endDate);
        booking.setTotalPrice(new BigDecimal(totalPrice));
        booking.setStatus(BookingStatus.CREATED);
        return booking;
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(String method, Object[] args);
    }

    private record Scenario(
            WalletService service,
            User owner,
            User sitter,
            WalletAccount ownerAccount,
            WalletAccount sitterAccount,
            Booking booking,
            AtomicReference<BookingPayment> payment,
            List<WalletTransaction> transactions
    ) {
        BookingPayment existingPayment(PaymentStatus status) {
            BookingPayment existing = new BookingPayment();
            existing.setId(UUID.randomUUID());
            existing.setBooking(booking);
            existing.setOwner(owner);
            existing.setSitter(sitter);
            existing.setAmount(booking.getTotalPrice());
            existing.setStatus(status);
            payment.set(existing);
            return existing;
        }
    }
}
