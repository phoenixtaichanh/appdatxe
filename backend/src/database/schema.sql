-- =====================================================
-- DoAn3 Database Schema
-- Hệ thống đặt xe thông minh kết hợp AI du lịch
-- =====================================================

CREATE DATABASE IF NOT EXISTS doan3_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE doan3_db;

-- =====================================================
-- CORE TABLES (5 tables)
-- =====================================================

-- 1. users - Người dùng (khách hàng, tài xế, admin)
CREATE TABLE IF NOT EXISTS users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    phone           VARCHAR(20) DEFAULT NULL,
    user_type       ENUM('passenger', 'driver', 'owner', 'consultant', 'hr_manager', 'revenue_manager')
                    DEFAULT 'passenger',
    profile_image   VARCHAR(500) DEFAULT NULL,
    rating          DECIMAL(3,2) DEFAULT 5.00,
    total_rides     INT DEFAULT 0,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. drivers - Hồ sơ tài xế (mở rộng từ users)
CREATE TABLE IF NOT EXISTS drivers (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL UNIQUE,
    car_model       VARCHAR(100) DEFAULT NULL,
    car_color       VARCHAR(50) DEFAULT NULL,
    license_plate   VARCHAR(20) DEFAULT NULL,
    is_available    BOOLEAN DEFAULT FALSE,
    current_ride_id INT DEFAULT NULL,
    latitude        DECIMAL(10, 8) DEFAULT NULL,
    longitude       DECIMAL(11, 8) DEFAULT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_available (is_available),
    INDEX idx_location (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. rides - Chuyến đi
CREATE TABLE IF NOT EXISTS rides (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    passenger_id        INT NOT NULL,
    driver_id           INT DEFAULT NULL,
    pickup_lat         DECIMAL(10, 8) NOT NULL,
    pickup_lng         DECIMAL(11, 8) NOT NULL,
    pickup_address     VARCHAR(500) DEFAULT '',
    dest_lat           DECIMAL(10, 8) NOT NULL,
    dest_lng           DECIMAL(11, 8) NOT NULL,
    dest_address       VARCHAR(500) DEFAULT '',
    vehicle_type       ENUM('motorbike', 'car_4_seats', 'car_7_seats') DEFAULT 'motorbike',
    distance_km         DECIMAL(8, 2) DEFAULT NULL,
    duration_min       INT DEFAULT NULL,
    price              DECIMAL(10, 0) DEFAULT 0,
    status             ENUM('pending', 'accepted', 'arrived', 'in_progress', 'completed', 'cancelled')
                        DEFAULT 'pending',
    driver_rating      TINYINT DEFAULT NULL,
    passenger_rating  TINYINT DEFAULT NULL,
    rating_comment     VARCHAR(500) DEFAULT NULL,
    started_at         TIMESTAMP NULL DEFAULT NULL,
    completed_at       TIMESTAMP NULL DEFAULT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (passenger_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_passenger (passenger_id),
    INDEX idx_driver (driver_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. driver_locations - Vị trí tài xế (real-time)
CREATE TABLE IF NOT EXISTS driver_locations (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    driver_id   INT NOT NULL,
    latitude    DECIMAL(10, 8) NOT NULL,
    longitude   DECIMAL(11, 8) NOT NULL,
    accuracy    DECIMAL(6, 2) DEFAULT NULL,
    speed       DECIMAL(6, 2) DEFAULT NULL,
    heading     INT DEFAULT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_driver (driver_id),
    INDEX idx_updated (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. earnings - Thu nhập tài xế
CREATE TABLE IF NOT EXISTS earnings (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    driver_id   INT NOT NULL,
    ride_id     INT DEFAULT NULL,
    amount      DECIMAL(12, 0) NOT NULL,
    type        ENUM('ride', 'bonus', 'penalty', 'withdrawal') DEFAULT 'ride',
    note        VARCHAR(255) DEFAULT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE SET NULL,
    INDEX idx_driver_date (driver_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- AI TABLES (6 tables)
-- =====================================================

-- 6. ai_trip_schedules - Lịch trình AI
CREATE TABLE IF NOT EXISTS ai_trip_schedules (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    user_id                 INT NOT NULL,
    schedule_name           VARCHAR(255) NOT NULL,
    scheduled_date         DATE DEFAULT NULL,
    total_estimated_time   INT DEFAULT NULL COMMENT 'Minutes',
    total_estimated_price   DECIMAL(12, 0) DEFAULT 0,
    total_distance         DECIMAL(8, 2) DEFAULT NULL COMMENT 'km',
    optimization_type       ENUM('time', 'cost', 'balanced') DEFAULT 'balanced',
    ai_confidence_score   DECIMAL(3,2) DEFAULT 0.85,
    traffic_condition      VARCHAR(50) DEFAULT NULL,
    status                 ENUM('planned', 'in_progress', 'completed', 'cancelled') DEFAULT 'planned',
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_date (scheduled_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. ai_waypoints - Điểm dừng trong lịch trình
CREATE TABLE IF NOT EXISTS ai_waypoints (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    schedule_id             INT NOT NULL,
    stop_order              INT NOT NULL,
    stop_type               ENUM('pickup', 'dropoff', 'stopover') DEFAULT 'stopover',
    latitude                DECIMAL(10, 8) NOT NULL,
    longitude               DECIMAL(11, 8) NOT NULL,
    address                 VARCHAR(500) DEFAULT '',
    stop_name               VARCHAR(255) DEFAULT NULL,
    estimated_arrival       TIME DEFAULT NULL,
    estimated_departure     TIME DEFAULT NULL,
    duration_min            INT DEFAULT 0 COMMENT 'Thời gian dừng (phút)',
    distance_from_prev      DECIMAL(8, 2) DEFAULT NULL COMMENT 'km',
    estimated_price_segment DECIMAL(12, 0) DEFAULT 0,
    is_optional             BOOLEAN DEFAULT FALSE,
    priority                INT DEFAULT 0,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES ai_trip_schedules(id) ON DELETE CASCADE,
    INDEX idx_schedule (schedule_id),
    INDEX idx_order (stop_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. ai_route_alternatives - Tuyến đường thay thế
CREATE TABLE IF NOT EXISTS ai_route_alternatives (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    schedule_id         INT NOT NULL,
    route_name          VARCHAR(100) NOT NULL,
    total_distance      DECIMAL(8, 2) NOT NULL COMMENT 'km',
    total_duration      INT NOT NULL COMMENT 'Minutes',
    total_price         DECIMAL(12, 0) NOT NULL,
    route_description    TEXT DEFAULT NULL,
    is_recommended      BOOLEAN DEFAULT FALSE,
    traffic_scenario    VARCHAR(50) DEFAULT NULL COMMENT 'morning_peak, evening_peak, typical, light',
    weather_impact      DECIMAL(3,2) DEFAULT 0.0 COMMENT '0.0 - 1.0',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES ai_trip_schedules(id) ON DELETE CASCADE,
    INDEX idx_schedule (schedule_id),
    INDEX idx_recommended (is_recommended)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. ai_learning_profiles - Hồ sơ học tập AI
CREATE TABLE IF NOT EXISTS ai_learning_profiles (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    user_id                     INT NOT NULL UNIQUE,
    preferred_time_start         TIME DEFAULT NULL,
    preferred_time_end           TIME DEFAULT NULL,
    average_trip_duration        DECIMAL(6, 2) DEFAULT 0,
    average_trip_cost           DECIMAL(12, 0) DEFAULT 0,
    total_distance_travelled    DECIMAL(10, 2) DEFAULT 0 COMMENT 'km',
    peak_hours_pattern           VARCHAR(100) DEFAULT NULL COMMENT 'JSON pattern data',
    frequent_locations           TEXT DEFAULT NULL COMMENT 'JSON array of locations',
    avoid_locations              TEXT DEFAULT NULL COMMENT 'JSON array of locations to avoid',
    preference_cost_vs_time      DECIMAL(3,2) DEFAULT 0.50 COMMENT '0.0 = time priority, 1.0 = cost priority',
    model_version                VARCHAR(20) DEFAULT 'v1.0',
    created_at                   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. driver_route_batches - Lịch trình gom chuyến
CREATE TABLE IF NOT EXISTS driver_route_batches (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    driver_id               INT NOT NULL,
    batch_name              VARCHAR(255) DEFAULT NULL,
    status                  ENUM('proposed', 'accepted', 'rejected', 'completed', 'cancelled') DEFAULT 'proposed',
    total_revenue          DECIMAL(12, 0) DEFAULT 0,
    total_distance         DECIMAL(8, 2) DEFAULT NULL COMMENT 'km',
    passenger_count        INT DEFAULT 0,
    efficiency_score        DECIMAL(4, 2) DEFAULT 0 COMMENT '0.00 - 1.00',
    ai_confidence          DECIMAL(3, 2) DEFAULT 0.85,
    estimated_start_time   TIME DEFAULT NULL,
    estimated_end_time      TIME DEFAULT NULL,
    accepted_at            TIMESTAMP NULL DEFAULT NULL,
    completed_at           TIMESTAMP NULL DEFAULT NULL,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_driver (driver_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. batch_passengers - Hành khách trong batch
CREATE TABLE IF NOT EXISTS batch_passengers (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    batch_id                INT NOT NULL,
    passenger_id            INT NOT NULL,
    original_ride_id         INT NOT NULL,
    pickup_order            INT DEFAULT 1,
    dropoff_order           INT DEFAULT 2,
    pickup_lat              DECIMAL(10, 8) NOT NULL,
    pickup_lng              DECIMAL(11, 8) NOT NULL,
    dropoff_lat             DECIMAL(10, 8) NOT NULL,
    dropoff_lng             DECIMAL(11, 8) NOT NULL,
    estimated_pickup_time   TIME DEFAULT NULL,
    detour_km               DECIMAL(6, 2) DEFAULT 0,
    price_adjustment        DECIMAL(12, 0) DEFAULT 0 COMMENT 'Giảm giá so với giá gốc',
    status                  ENUM('pending', 'picked_up', 'dropped_off', 'cancelled') DEFAULT 'pending',
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES driver_route_batches(id) ON DELETE CASCADE,
    FOREIGN KEY (passenger_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (original_ride_id) REFERENCES rides(id) ON DELETE CASCADE,
    INDEX idx_batch (batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TRIGGERS: Tự động cập nhật total_rides cho user
-- =====================================================

DELIMITER //

CREATE TRIGGER after_ride_completed_passenger
AFTER UPDATE ON rides
FOR EACH ROW
BEGIN
    IF NEW.status = 'completed' AND OLD.status != 'completed' THEN
        UPDATE users SET total_rides = total_rides + 1 WHERE id = NEW.passenger_id;
    END IF;
END//

CREATE TRIGGER after_ride_completed_driver
AFTER UPDATE ON rides
FOR EACH ROW
BEGIN
    IF NEW.status = 'completed' AND OLD.status != 'completed' AND NEW.driver_id IS NOT NULL THEN
        UPDATE users SET total_rides = total_rides + 1 WHERE id = NEW.driver_id;
    END IF;
END//

DELIMITER ;

-- =====================================================
-- ADDITIONAL TABLES
-- =====================================================

-- ride_rating_tags: Tags for ride ratings (enhanced rating)
CREATE TABLE IF NOT EXISTS ride_rating_tags (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    ride_id         INT NOT NULL,
    tag             VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE CASCADE,
    INDEX idx_ride (ride_id),
    INDEX idx_tag (tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- cancellation_log: Log of ride cancellations with reasons
CREATE TABLE IF NOT EXISTS cancellation_log (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    ride_id         INT NOT NULL,
    cancelled_by    ENUM('passenger', 'driver', 'system') NOT NULL,
    user_id         INT NOT NULL,
    reason          VARCHAR(255) DEFAULT NULL,
    cancellation_fee DECIMAL(12,0) DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE CASCADE,
    INDEX idx_ride (ride_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ride_images: Images related to rides (damage claims, receipts)
CREATE TABLE IF NOT EXISTS ride_images (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    ride_id         INT NOT NULL,
    image_url       VARCHAR(500) NOT NULL,
    image_type      ENUM('damage', 'receipt', 'other') DEFAULT 'other',
    uploaded_by     INT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE CASCADE,
    INDEX idx_ride (ride_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- password_resets: OTP codes for password reset
CREATE TABLE IF NOT EXISTS password_resets (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    otp_code        VARCHAR(6) NOT NULL,
    expires_at      TIMESTAMP NOT NULL,
    is_used         BOOLEAN DEFAULT FALSE,
    used_at         TIMESTAMP NULL DEFAULT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_otp (otp_code),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- transactions: Payment transactions (VNPay, MoMo, Cash, Wallet)
CREATE TABLE IF NOT EXISTS transactions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    ride_id         INT DEFAULT NULL,
    type            ENUM('income', 'expense', 'refund', 'bonus', 'penalty') DEFAULT 'income',
    category        VARCHAR(50) DEFAULT NULL COMMENT 'ride_fare, cancellation_fee, bonus, etc.',
    amount          DECIMAL(12, 0) NOT NULL,
    payment_method  ENUM('cash', 'wallet', 'vnpay', 'momo') DEFAULT 'cash',
    status          ENUM('pending', 'completed', 'failed', 'refunded', 'cancelled') DEFAULT 'pending',
    description     VARCHAR(500) DEFAULT NULL,
    payment_ref     VARCHAR(255) DEFAULT NULL COMMENT 'VNPay/MoMo transaction reference',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_ride (ride_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- user_fcm_tokens: FCM push notification tokens
CREATE TABLE IF NOT EXISTS user_fcm_tokens (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    fcm_token       VARCHAR(500) NOT NULL,
    device_id       VARCHAR(255) DEFAULT NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    UNIQUE INDEX idx_token (fcm_token(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
