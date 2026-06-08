package com.petcare.booking.service.impl;

import com.petcare.booking.entity.ServiceBooking;
import com.petcare.booking.service.BookingRetryService;
import com.petcare.booking.service.BookingTransactionService;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;

/**
 * Retries booking transactions on deadlock or lock timeout.
 * Separated from BookingTransactionService to avoid self-invocation proxy issues.
 */
@Service
public class BookingRetryServiceImpl implements BookingRetryService {

    private static final Logger log = LoggerFactory.getLogger(BookingRetryServiceImpl.class);
    private static final int MAX_RETRIES = 2;

    private final BookingTransactionService bookingTransactionService;

    public BookingRetryServiceImpl(BookingTransactionService bookingTransactionService) {
        this.bookingTransactionService = bookingTransactionService;
    }

    @Override
    public ServiceBooking createBookingWithRetry(ServiceBooking booking) {
        int attempts = 0;
        BusinessException lastException = null;

        while (attempts <= MAX_RETRIES) {
            try {
                return bookingTransactionService.createBookingOnce(booking);
            } catch (DeadlockLoserDataAccessException e) {
                attempts++;
                lastException = new BusinessException(ErrorCode.BOOKING_RETRY_EXHAUSTED,
                        "预约创建遇到死锁，请重试");
                log.warn("Deadlock on booking attempt {}/{}: staffId={}, date={}",
                        attempts, MAX_RETRIES, booking.getStaffId(), booking.getBookingDate());
                if (attempts > MAX_RETRIES) break;
                sleepRandom();
            } catch (PessimisticLockingFailureException e) {
                attempts++;
                lastException = new BusinessException(ErrorCode.BOOKING_RETRY_EXHAUSTED,
                        "预约创建遇到锁等待超时，请重试");
                log.warn("Lock timeout on booking attempt {}/{}: staffId={}, date={}",
                        attempts, MAX_RETRIES, booking.getStaffId(), booking.getBookingDate());
                if (attempts > MAX_RETRIES) break;
                sleepRandom();
            }
        }

        throw lastException != null ? lastException :
                new BusinessException(ErrorCode.BOOKING_RETRY_EXHAUSTED, "预约创建重试耗尽，请稍后重试");
    }

    private void sleepRandom() {
        try {
            long delay = 50 + (long) (Math.random() * 150); // 50-200ms
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
