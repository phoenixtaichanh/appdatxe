-- ============================================================
-- DoAn3 Full Database Setup
-- Paste entire file into MySQL Workbench query editor and run
-- Mobile test accounts: passenger@test.com / driver1@test.com - password: password123
-- Admin panel accounts: owner@doan3.vn / admin@doan3.vn - password: Admin@123
-- ============================================================

CREATE DATABASE IF NOT EXISTS doan3_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE doan3_db;

-- CORE TABLES

CREATE TABLE IF NOT EXISTS users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    phone           VARCHAR(20) DEFAULT NULL,
    user_type       ENUM('passenger', 'driver', 'owner', 'consultant', 'hr_manager', 'revenue_manager') DEFAULT 'passenger',
    profile_image   VARCHAR(500) DEFAULT NULL,
    rating          DECIMAL(3,2) DEFAULT 5.00,
    total_rides     INT DEFAULT 0,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE IF NOT EXISTS rides (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    passenger_id        INT NOT NULL,
    driver_id           INT DEFAULT NULL,
    pickup_lat          DECIMAL(10, 8) NOT NULL,
    pickup_lng          DECIMAL(11, 8) NOT NULL,
    pickup_address      VARCHAR(500) DEFAULT '',
    dest_lat            DECIMAL(10, 8) NOT NULL,
    dest_lng            DECIMAL(11, 8) NOT NULL,
    dest_address        VARCHAR(500) DEFAULT '',
    vehicle_type        ENUM('motorbike', 'car_4_seats', 'car_7_seats') DEFAULT 'motorbike',
    distance_km         DECIMAL(8, 2) DEFAULT NULL,
    duration_min        INT DEFAULT NULL,
    price               DECIMAL(10, 0) DEFAULT 0,
    status              ENUM('pending', 'accepted', 'arrived', 'in_progress', 'completed', 'cancelled') DEFAULT 'pending',
    driver_rating       TINYINT DEFAULT NULL,
    passenger_rating    TINYINT DEFAULT NULL,
    rating_comment      VARCHAR(500) DEFAULT NULL,
    started_at          TIMESTAMP NULL DEFAULT NULL,
    completed_at        TIMESTAMP NULL DEFAULT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (passenger_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_passenger (passenger_id),
    INDEX idx_driver (driver_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS driver_locations (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    driver_id   INT NOT NULL,
    latitude    DECIMAL(10, 8) NOT NULL,
    longitude   DECIMAL(11, 8) NOT NULL,
    accuracy    DECIMAL(6, 2) DEFAULT NULL,
    speed       DECIMAL(6, 2) DEFAULT NULL,
    heading     INT DEFAULT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE,
    INDEX idx_driver (driver_id),
    INDEX idx_updated (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS earnings (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    driver_id   INT NOT NULL,
    ride_id     INT DEFAULT NULL,
    amount      DECIMAL(12, 0) NOT NULL,
    type        ENUM('ride', 'bonus', 'penalty', 'withdrawal') DEFAULT 'ride',
    note        VARCHAR(255) DEFAULT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE,
    FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE SET NULL,
    INDEX idx_driver_date (driver_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI TABLES

CREATE TABLE IF NOT EXISTS ai_trip_schedules (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    user_id                 INT NOT NULL,
    schedule_name           VARCHAR(255) NOT NULL,
    scheduled_date          DATE DEFAULT NULL,
    total_estimated_time    INT DEFAULT NULL,
    total_estimated_price   DECIMAL(12, 0) DEFAULT 0,
    total_distance          DECIMAL(8, 2) DEFAULT NULL,
    optimization_type       ENUM('time', 'cost', 'balanced') DEFAULT 'balanced',
    ai_confidence_score     DECIMAL(3,2) DEFAULT 0.85,
    traffic_condition       VARCHAR(50) DEFAULT NULL,
    status                  ENUM('planned', 'in_progress', 'completed', 'cancelled') DEFAULT 'planned',
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_date (scheduled_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    duration_min            INT DEFAULT 0,
    distance_from_prev      DECIMAL(8, 2) DEFAULT NULL,
    estimated_price_segment DECIMAL(12, 0) DEFAULT 0,
    is_optional             BOOLEAN DEFAULT FALSE,
    priority                INT DEFAULT 0,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES ai_trip_schedules(id) ON DELETE CASCADE,
    INDEX idx_schedule (schedule_id),
    INDEX idx_order (stop_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_route_alternatives (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    schedule_id         INT NOT NULL,
    route_name          VARCHAR(100) NOT NULL,
    total_distance      DECIMAL(8, 2) NOT NULL,
    total_duration      INT NOT NULL,
    total_price         DECIMAL(12, 0) NOT NULL,
    route_description   TEXT DEFAULT NULL,
    is_recommended      BOOLEAN DEFAULT FALSE,
    traffic_scenario    VARCHAR(50) DEFAULT NULL,
    weather_impact      DECIMAL(3,2) DEFAULT 0.0,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES ai_trip_schedules(id) ON DELETE CASCADE,
    INDEX idx_schedule (schedule_id),
    INDEX idx_recommended (is_recommended)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_learning_profiles (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    user_id                     INT NOT NULL UNIQUE,
    preferred_time_start        TIME DEFAULT NULL,
    preferred_time_end          TIME DEFAULT NULL,
    average_trip_duration       DECIMAL(6, 2) DEFAULT 0,
    average_trip_cost           DECIMAL(12, 0) DEFAULT 0,
    total_distance_travelled    DECIMAL(10, 2) DEFAULT 0,
    peak_hours_pattern          VARCHAR(100) DEFAULT NULL,
    frequent_locations          TEXT DEFAULT NULL,
    avoid_locations             TEXT DEFAULT NULL,
    preference_cost_vs_time     DECIMAL(3,2) DEFAULT 0.50,
    model_version               VARCHAR(20) DEFAULT 'v1.0',
    created_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS driver_route_batches (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    driver_id               INT NOT NULL,
    batch_name              VARCHAR(255) DEFAULT NULL,
    status                  ENUM('proposed', 'accepted', 'rejected', 'completed', 'cancelled') DEFAULT 'proposed',
    total_revenue           DECIMAL(12, 0) DEFAULT 0,
    total_distance          DECIMAL(8, 2) DEFAULT NULL,
    passenger_count         INT DEFAULT 0,
    efficiency_score        DECIMAL(4, 2) DEFAULT 0,
    ai_confidence           DECIMAL(3, 2) DEFAULT 0.85,
    estimated_start_time    TIME DEFAULT NULL,
    estimated_end_time      TIME DEFAULT NULL,
    accepted_at             TIMESTAMP NULL DEFAULT NULL,
    completed_at            TIMESTAMP NULL DEFAULT NULL,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE,
    INDEX idx_driver (driver_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS batch_passengers (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    batch_id                INT NOT NULL,
    passenger_id            INT NOT NULL,
    original_ride_id        INT NOT NULL,
    pickup_order            INT DEFAULT 1,
    dropoff_order           INT DEFAULT 2,
    pickup_lat              DECIMAL(10, 8) NOT NULL,
    pickup_lng              DECIMAL(11, 8) NOT NULL,
    dropoff_lat             DECIMAL(10, 8) NOT NULL,
    dropoff_lng             DECIMAL(11, 8) NOT NULL,
    estimated_pickup_time   TIME DEFAULT NULL,
    detour_km               DECIMAL(6, 2) DEFAULT 0,
    price_adjustment        DECIMAL(12, 0) DEFAULT 0,
    status                  ENUM('pending', 'picked_up', 'dropped_off', 'cancelled') DEFAULT 'pending',
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES driver_route_batches(id) ON DELETE CASCADE,
    FOREIGN KEY (passenger_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (original_ride_id) REFERENCES rides(id) ON DELETE CASCADE,
    INDEX idx_batch (batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ADDITIONAL TABLES

CREATE TABLE IF NOT EXISTS ride_rating_tags (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    ride_id         INT NOT NULL,
    tag             VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE CASCADE,
    INDEX idx_ride (ride_id),
    INDEX idx_tag (tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE IF NOT EXISTS transactions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    ride_id         INT DEFAULT NULL,
    type            ENUM('income', 'expense', 'refund', 'bonus', 'penalty') DEFAULT 'income',
    category        VARCHAR(50) DEFAULT NULL,
    amount          DECIMAL(12, 0) NOT NULL,
    payment_method  ENUM('cash', 'wallet', 'vnpay', 'momo') DEFAULT 'cash',
    status          ENUM('pending', 'completed', 'failed', 'refunded', 'cancelled') DEFAULT 'pending',
    description     VARCHAR(500) DEFAULT NULL,
    payment_ref     VARCHAR(255) DEFAULT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_ride (ride_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_fcm_tokens (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    fcm_token       VARCHAR(500) NOT NULL,
    device_id       VARCHAR(255) DEFAULT NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SUPPORT SYSTEM TABLES

CREATE TABLE IF NOT EXISTS faqs (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    category            VARCHAR(100) NOT NULL DEFAULT 'general',
    question            VARCHAR(500) NOT NULL,
    answer              TEXT NOT NULL,
    display_order       INT DEFAULT 0,
    is_active           BOOLEAN DEFAULT TRUE,
    view_count          INT DEFAULT 0,
    helpful_count       INT DEFAULT 0,
    unhelpful_count     INT DEFAULT 0,
    created_by          INT DEFAULT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_active (is_active),
    INDEX idx_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS consultant_conversations (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    customer_id         INT NOT NULL,
    consultant_id       INT DEFAULT NULL,
    subject             VARCHAR(255) DEFAULT NULL,
    category            VARCHAR(50) DEFAULT 'general',
    status              ENUM('waiting', 'active', 'resolved', 'closed', 'escalated') DEFAULT 'waiting',
    priority            ENUM('low', 'normal', 'high', 'urgent') DEFAULT 'normal',
    customer_rating     TINYINT DEFAULT NULL,
    customer_feedback   VARCHAR(500) DEFAULT NULL,
    resolved_at         TIMESTAMP NULL DEFAULT NULL,
    closed_at           TIMESTAMP NULL DEFAULT NULL,
    first_response_at   TIMESTAMP NULL DEFAULT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (consultant_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_customer (customer_id),
    INDEX idx_consultant (consultant_id),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_priority (priority),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS consultant_messages (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    conversation_id     INT NOT NULL,
    sender_id           INT NOT NULL,
    sender_type         ENUM('customer', 'consultant', 'system') NOT NULL,
    sender_name         VARCHAR(255) NOT NULL,
    message             TEXT NOT NULL,
    message_type        ENUM('text', 'image', 'file', 'system') DEFAULT 'text',
    attachment_url      VARCHAR(500) DEFAULT NULL,
    is_read             BOOLEAN DEFAULT FALSE,
    read_at             TIMESTAMP NULL DEFAULT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES consultant_conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_conversation (conversation_id),
    INDEX idx_sender (sender_id),
    INDEX idx_read (is_read),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_sessions (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    user_id             INT NOT NULL,
    device_id           VARCHAR(255) DEFAULT NULL,
    device_name         VARCHAR(255) DEFAULT NULL,
    device_platform     VARCHAR(50) DEFAULT NULL,
    ip_address          VARCHAR(45) DEFAULT NULL,
    fcm_token           VARCHAR(500) DEFAULT NULL,
    last_active_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active           BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS consultant_availability (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    consultant_id           INT NOT NULL UNIQUE,
    is_available            BOOLEAN DEFAULT FALSE,
    max_conversations       INT DEFAULT 10,
    current_conversations   INT DEFAULT 0,
    auto_assign             BOOLEAN DEFAULT TRUE,
    working_hours_start     TIME DEFAULT '08:00:00',
    working_hours_end       TIME DEFAULT '22:00:00',
    last_ping_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (consultant_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_available (is_available)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification_log (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    user_id             INT NOT NULL,
    type                VARCHAR(50) DEFAULT 'info',
    title               VARCHAR(255) NOT NULL,
    body                VARCHAR(500) DEFAULT NULL,
    data                JSON DEFAULT NULL,
    is_read             BOOLEAN DEFAULT FALSE,
    read_at             TIMESTAMP NULL DEFAULT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_read (is_read),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ADMIN PANEL RBAC TABLES

CREATE TABLE IF NOT EXISTS admin_roles (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    role_key    VARCHAR(50) NOT NULL UNIQUE,
    role_name   VARCHAR(100) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS admin_permissions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    permission_key  VARCHAR(100) NOT NULL UNIQUE,
    permission_name VARCHAR(200) NOT NULL,
    group_name      VARCHAR(100) NOT NULL DEFAULT 'Chung',
    description     VARCHAR(500) DEFAULT NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_permission_key (permission_key),
    INDEX idx_group (group_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS admin_role_permissions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    role_id         INT NOT NULL,
    permission_id   INT NOT NULL,
    is_granted      BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_role_permission (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES admin_roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES admin_permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS admin_users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    phone           VARCHAR(20) DEFAULT NULL,
    avatar_url      VARCHAR(500) DEFAULT NULL,
    role_id         INT NOT NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    last_login_at   DATETIME DEFAULT NULL,
    last_login_ip   VARCHAR(45) DEFAULT NULL,
    created_by      INT DEFAULT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role_id),
    INDEX idx_active (is_active),
    FOREIGN KEY (role_id) REFERENCES admin_roles(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS admin_sessions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    admin_user_id   INT NOT NULL,
    token           VARCHAR(500) NOT NULL UNIQUE,
    ip_address      VARCHAR(45) DEFAULT NULL,
    user_agent      VARCHAR(500) DEFAULT NULL,
    expires_at      DATETIME NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_token (token),
    INDEX idx_admin (admin_user_id),
    INDEX idx_expires (expires_at),
    FOREIGN KEY (admin_user_id) REFERENCES admin_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TRIGGERS (paste these separately after tables are created)

DROP TRIGGER IF EXISTS after_ride_completed_passenger;
CREATE TRIGGER after_ride_completed_passenger AFTER UPDATE ON rides FOR EACH ROW UPDATE users SET total_rides = total_rides + 1 WHERE id = NEW.passenger_id AND NEW.status = 'completed' AND OLD.status != 'completed';

DROP TRIGGER IF EXISTS after_ride_completed_driver;
CREATE TRIGGER after_ride_completed_driver AFTER UPDATE ON rides FOR EACH ROW UPDATE users SET total_rides = total_rides + 1 WHERE id = NEW.driver_id AND NEW.driver_id IS NOT NULL AND NEW.status = 'completed' AND OLD.status != 'completed';

-- SEED: MOBILE ACCOUNTS (password: password123)

INSERT INTO users (email, password, name, phone, user_type, rating, total_rides) VALUES
    ('passenger@test.com', '$2a$10$p4lsVUZpJWzIse4I7Yf/Q.U8i1JDqU2N32LBzUuASNKj9u7dQmh9i', 'Nguyen Van Test', '0909123456', 'passenger', 5.00, 0),
    ('passenger@gmail.com', '$2a$10$p4lsVUZpJWzIse4I7Yf/Q.U8i1JDqU2N32LBzUuASNKj9u7dQmh9i', 'Nguyen Van Khach', '0909123457', 'passenger', 5.00, 0),
    ('driver1@test.com', '$2a$10$p4lsVUZpJWzIse4I7Yf/Q.U8i1JDqU2N32LBzUuASNKj9u7dQmh9i', 'Tran Van Tai 1', '0909234567', 'driver', 4.80, 42),
    ('driver1@gmail.com', '$2a$10$p4lsVUZpJWzIse4I7Yf/Q.U8i1JDqU2N32LBzUuASNKj9u7dQmh9i', 'Tran Van Tai 1', '0909234567', 'driver', 4.80, 42),
    ('driver@gmail.com', '$2a$10$p4lsVUZpJWzIse4I7Yf/Q.U8i1JDqU2N32LBzUuASNKj9u7dQmh9i', 'Tai Xe Mac Dinh', '0909234568', 'driver', 4.90, 35),
    ('driver2@test.com', '$2a$10$p4lsVUZpJWzIse4I7Yf/Q.U8i1JDqU2N32LBzUuASNKj9u7dQmh9i', 'Le Thi Xe 2', '0909345678', 'driver', 4.65, 28),
    ('driver3@test.com', '$2a$10$p4lsVUZpJWzIse4I7Yf/Q.U8i1JDqU2N32LBzUuASNKj9u7dQmh9i', 'Pham Xuan Lai 3', '0909456789', 'driver', 4.92, 65),
    ('consultant@doan3.vn', '$2a$10$NQswCX7hQU/ruxwizDs6YeM7oc1WozDlBh9gsiulT3a18reIARI2W', 'Tran Thi Tu Van', '0909555666', 'consultant', 5.00, 0)
ON DUPLICATE KEY UPDATE email = email;

INSERT INTO drivers (user_id, car_model, car_color, license_plate, is_available, latitude, longitude) VALUES
    (2, 'Toyota Camry', 'Den', '43A-123.45', TRUE, 16.0544, 108.2022),
    (3, 'Honda Vision', 'Trang', '43L-567.89', TRUE, 16.0600, 108.2100),
    (4, 'Ford Everest', 'Bac', '43K-999.99', TRUE, 16.0700, 108.2200)
ON DUPLICATE KEY UPDATE user_id = user_id;

INSERT INTO rides (passenger_id, driver_id, pickup_lat, pickup_lng, pickup_address, dest_lat, dest_lng, dest_address, distance_km, duration_min, price, status, driver_rating, created_at) VALUES
    (1, 2, 16.0544, 108.2022, 'San bay Da Nang', 16.0678, 108.2100, 'Truong DH Bach Khoa Da Nang', 2.5, 8, 24000, 'completed', 5, DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (1, 3, 16.0544, 108.2022, 'San bay Da Nang', 16.0800, 108.2300, 'Bai Bien My Khe', 4.2, 12, 31000, 'completed', 4, DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (1, NULL, 16.0600, 108.2100, 'Truong DH Bach Khoa', 16.0544, 108.2022, 'San bay Da Nang', 1.8, 6, 19800, 'pending', NULL, NOW())
ON DUPLICATE KEY UPDATE passenger_id = passenger_id;

INSERT INTO driver_locations (driver_id, latitude, longitude, updated_at) VALUES
    (1, 16.0545, 108.2023, NOW()),
    (2, 16.0600, 108.2100, NOW()),
    (3, 16.0700, 108.2200, NOW())
ON DUPLICATE KEY UPDATE driver_id = driver_id;

INSERT INTO earnings (driver_id, ride_id, amount, type, created_at) VALUES
    (1, 1, 24000, 'ride', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (2, 2, 31000, 'ride', DATE_SUB(NOW(), INTERVAL 1 DAY))
ON DUPLICATE KEY UPDATE driver_id = driver_id;

INSERT INTO ai_learning_profiles (user_id, preferred_time_start, preferred_time_end, average_trip_duration, average_trip_cost, total_distance_travelled, preference_cost_vs_time, model_version) VALUES
    (1, '08:00:00', '20:00:00', 25.5, 65000, 450.5, 0.60, 'v1.1'),
    (2, '06:00:00', '22:00:00', 0, 0, 0, 0.50, 'v1.0'),
    (3, '06:00:00', '22:00:00', 0, 0, 0, 0.50, 'v1.0'),
    (4, '06:00:00', '22:00:00', 0, 0, 0, 0.50, 'v1.0')
ON DUPLICATE KEY UPDATE user_id = user_id;

-- SEED: FAQ

INSERT INTO faqs (category, question, answer, display_order) VALUES
-- GENERAL
('general', 'Ung dung DoAn3 ho tro nhung loai phuong tien nao?',
'DoAn3 hien ho tro 3 loai phuong tien:\n1. Xe may - Phu hop cho di chuyen ngan (tu 10.000d co ban + 3.000d/km)\n2. O to 4 cho - Phu hop cho gia dinh nho (tu 12.000d co ban + 5.000d/km)\n3. O to 7 cho - Phu hop cho nhom dong nguoi (tu 15.000d co ban + 7.000d/km)', 1),
('general', 'DoAn3 khac gi cac ung dung dat xe khac?',
'1. Tich hop AI thong minh - goi y lich trinh toi uu\n2. Ghep chuyen (Batch) - tiet kiem chi phi\n3. Theo doi thoi quen di chuyen\n4. Ho tro 24/7\n5. Da dang phuong tien', 2),
('general', 'Toi co the su dung DoAn3 o bat ky dia diem nao khong?',
'Hien tai DoAn3 dang phuc vu tai khu vuc Da Nang. Chung toi se mo rong them nhieu tinh thanh trong thoi gian toi. Ban co the theo doi thong tin cap nhat tren ung dung.', 3),
('general', 'DoAn3 co ho tro khach hang khong biet tieng Viet khong?',
'DoAn3 co the hien thi mot so noi dung bang tieng Anh. Tuy nhien, de duoc ho tro tot nhat, ban vui long lien he nhom cham soc khach hang qua chat trong ung dung.', 4),
-- BOOKING
('booking', 'Lam sao de dat xe tren DoAn3?',
'1. Mo ung dung va dang nhap\n2. Nhan "Dat xe ngay"\n3. Chon diem don va diem den tren ban do\n4. Chon loai phuong tien (Xe may, O to 4 cho, O to 7 cho)\n5. Nhan "Tim tai xe" de xem gia uoc tinh\n6. Nhan "Dat xe ngay" de gui yeu cau', 5),
('booking', 'Toi co the dat xe truoc bao lau?',
'Ban co the dat xe ngay lap tuc hoac dat truoc trong vong 7 ngay. De dat xe truoc, chon ngay va gio tai buoc chon thoi gian khi dat xe.', 6),
('booking', 'Toi co the huy chuyen khong? Phi huy la bao nhieu?',
'Ban co the huy chuyen truoc khi tai xe bat dau.\n- Huy truoc khi tai xe nhan: Khong mat phi\n- Huy sau khi tai xe nhan: 10.000d - 20.000d\n- Huy sau khi tai xe da den: 30.000d - 50.000d', 7),
('booking', 'Lam sao de danh gia tai xe sau chuyen di?',
'Sau khi chuyen di ket thuc, man hinh danh gia se xuat hien tu dong. Ban co the:\n1. Chon so sao tu 1 den 5\n2. Chon cac tag phu hop (an toan, than thien, xe sach, ...)\n3. Viet nhan xet them neu muon', 8),
('booking', 'Tai xe co bat buoc cho toi khong?',
'Co, tai xe bat buoc cho toi tai diem don cua ban. Neu tai xe khong cho, ban co the:\n1. Lien he tai xe qua cuoc goi hoac tin nhan trong ung dung\n2. Bao cao qua muc "Ho tro" trong ung dung\n3. Danh gia tai xe sau chuyen di', 9),
('booking', 'Gia cuoc chang di duoc tinh nhu the nao?',
'Gia cuoc chang = Gia co ban + (Khoang cach x km x Don gia theo km). Gia se duoc hien thi truoc khi ban xac nhan dat xe. Gia co ban tuy thuoc vao loai phuong tien ban chon.', 10),
-- PAYMENT
('payment', 'DoAn3 ho tro nhung phuong thuc thanh toan nao?',
'1. Tien mat (Cash) - Tra truc tiep cho tai xe\n2. VNPay - Thanh toan qua cong thanh toan VNPay\n3. MoMo - Thanh toan qua ung dung MoMo\n4. Vi trong ung dung (Wallet) - Nap tien vao vi de thanh toan nhanh hon', 11),
('payment', 'Lam sao nap tien vao vi trong ung dung?',
'1. Mo ung dung DoAn3\n2. Vao trang ca nhan\n3. Chon "Nap tien"\n4. Chon so tien muon nap\n5. Chon phuong thuc thanh toan\n6. Xac nhan nap tien\nTien se duoc cong vao vi ngay sau khi giao dich thanh cong.', 12),
('payment', 'Toi co the xuat hoa don khong?',
'Co, ban co the yeu cau xuat hoa don sau moi chuyen di. Vao "Lich su chuyen di", chon chuyen di, nhan "Xuat hoa don" va dien thong tin xuat hoa don. Hoa don se duoc gui qua email.', 13),
('payment', 'Tai xe co the doi phuong thuc thanh toan khong?',
'Phuong thuc thanh toan da duoc xac nhan truoc khi dat xe. Tuy nhien, trong mot so truong hop dac biet, tai xe co the hoi ban de doi phuong thuc. Ban co quyen tu choi neu khong dong y.', 14),
('payment', 'Phi cong them bao nhieu neu qua gio lam viec?',
'Hien tai DoAn3 khong ap dung phi cong them theo gio. Gia cuoc chang chi duoc tinh dua tren khoang cach va loai phuong tien. Tuy nhien, gia co the thay doi trong gio cao diem (7h-9h va 17h-19h) do nhu cau tang cao.', 15),
-- ACCOUNT
('account', 'Lam sao tao tai khoan tren DoAn3?',
'1. Tai va cai dat ung dung DoAn3 tu App Store hoac Google Play\n2. Mo ung dung, chon tab "Dang ky"\n3. Nhap thong tin: Ho ten, email, so dien thoai, mat khau\n4. Neu la tai xe, chon "Tai xe" va nhap them thong tin xe\n5. Nhan "Dang ky" de hoan tat', 16),
('account', 'Lam sao dat lai mat khau?',
'1. Nhan "Quen mat khau?" tai man hinh dang nhap\n2. Nhap email da dang ky\n3. Kiem tra email de lay ma OTP 6 so\n4. Nhap ma OTP va dat mat khau moi (toi thieu 6 ky tu)\n5. Nhan "Xac nhan" de hoan tat', 17),
('account', 'Toi co the doi email hoac so dien thoai khong?',
'Ban co the doi so dien thoai trong phan "Chinh sua ho so". Email lien ket voi tai khoan chi co the doi khi lien he nhom ho tro. Vui long bao mat thong tin tai khoan cua ban.', 18),
('account', 'Lam sao xoa tai khoan?',
'Hiện tại bạn có thể yêu cầu xóa tài khoản bằng cách liên hệ nhóm hỗ trợ qua mục "Hỗ trợ & FAQ" trong ứng dụng. Tài khoản của bạn sẽ được xử lý trong vòng 7 ngày làm việc.', 19),
-- DRIVER
('driver', 'Toi muon dang ky lam tai xe, can gi?',
'1. Tai khoan DoAn3 (dang ky nhu khach hang)\n2. Chon loai xe: Xe may, O to 4 cho, hoac O to 7 cho\n3. Nhap thong tin xe: Mau xe, bien so xe\n4. Giay phep lai xe hop le\n5. Hinh chân dung\nSau khi dang ky, tai khoan se duoc xem xet trong 1-2 ngay lam viec.', 20),
('driver', 'Thu nhap cua tai xe duoc tinh nhu the nao?',
'Tai xe nhan 80% gia tri cuoc chang (sau khi tru phi nen tang 20%).\nVD: Cuoc chang 50.000d -> Tai xe nhan 40.000d.\nThu nhap duoc cong vao tai khoan vao cuoi ngay va co the rut ve tai khoan ngan hang bat cu luc nao.', 21),
('driver', 'Toi co the choi xe khi khong online khong?',
'Co, ban hoan toan co the tat che do san sang (Offline) khi khong muon nhan chuyen. Che do online/offline la tuy chon cua ban, khong bat buoc phai online.', 22),
('driver', 'Phi nen tang la gi? Phi bao nhieu?',
'Phi nen tang (Commission) la phi DoAn3 thu de van hanh he thong. Hien tai phi nen tang la 20% tren moi cuoc chang. Phan con lai 80% la thu nhap cua tai xe.', 23),
-- TECHNICAL
('technical', 'Ung dung bi lag, lam sao?',
'1. Khoi dong lai ung dung\n2. Khoi dong lai dien thoai\n3. Kiem tra ket noi internet (Wifi hoac 4G)\n4. Xoa bo nho cache: Cai dat -> Ung dung -> DoAn3 -> Xoa cache\n5. Cap nhat ung dung len phien ban moi nhat tu App Store / Google Play', 24),
('technical', 'Tai sao vi tri tai xe tren ban do khong chinh xac?',
'1. Kiem tra quyen truy cap vi tri cua ung dung trong Cai dat dien thoai\n2. Tat che do tiet kiem pin vi no lam giam do chinh xac GPS\n3. Dam bao dien thoai co ket noi internet on dinh\n4. Khong cho dien thoai trong bao lo vi it kim loai\n5. Thu dong va mo lai GPS trong cai dat dien thoai', 25),
('technical', 'Ung dung bi crash, toat ra khi dang su dung?',
'1. Cap nhat ung dung len phien ban moi nhat\n2. Kiem tra bo nho trong cua dien thoai, giai phong neu can\n3. Lien he ho tro qua muc "Ho tro & FAQ" trong ung dung\n4. Gui ID thiet bi va mo ta loi giup nhom ky thuat xu ly nhanh hon', 26),
('technical', 'Toi khong nhan duoc thong bao tu ung dung?',
'1. Kiem tra quyen thong bao cua DoAn3 trong Cai dat dien thoai\n2. Tat che do "Khong lam phi" hoac "Tiet kiem pin" cho DoAn3\n3. Kiem tra ket noi internet\n4. Thu tat va bat lai thong bao tren dien thoai', 27),
('technical', 'Ban do khong tai duoc, lam sao?',
'1. Kiem tra ket noi internet\n2. Xoa bo nho cache cua ung dung DoAn3\n3. Dam bao ban co Google Maps hoac Google Play Services\n4. Thu cap nhat Google Maps tren dien thoai\n5. Khoi dong lai dien thoai va thu lai', 28);

-- SEED: ADMIN ROLES

INSERT INTO admin_roles (role_key, role_name, description) VALUES
('owner', 'Chu so huu', 'Toan quyen - quan ly he thong, tai khoan admin, bao cao, tai chinh'),
('admin', 'Quan tri vien', 'Quan ly nguoi dung, chuyen di, tai xe, FAQ, tu van, bao cao'),
('revenue_manager', 'Nhan vien tai chinh', 'Chi xem va quan ly doanh thu, thanh toan, bao cao tai chinh'),
('consultant', 'Nhan vien tu van', 'Chi tra loi chat tu van khach hang'),
('hr_manager', 'Nhan vien nhan su', 'Quan ly thong tin tai xe, duyet tai xe, ho so nhan vien')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- SEED: ADMIN PERMISSIONS

INSERT INTO admin_permissions (permission_key, permission_name, group_name) VALUES
('dashboard', 'Xem Dashboard', 'He thong'),
('system_settings', 'Cai dat he thong', 'He thong'),
('manage_admins', 'Quan ly tai khoan Admin', 'Tai khoan'),
('manage_users', 'Quan ly tai khoan Khach hang', 'Tai khoan'),
('manage_drivers', 'Quan ly tai khoan Tai xe', 'Tai khoan'),
('manage_rides', 'Quan ly chuyen di', 'Chuyen di'),
('view_rides', 'Xem danh sach chuyen di', 'Chuyen di'),
('view_revenue', 'Xem bao cao doanh thu', 'Tai chinh'),
('manage_payments', 'Quan ly thanh toan', 'Tai chinh'),
('export_reports', 'Xuat bao cao', 'Tai chinh'),
('view_consultant_chats', 'Xem chat tu van', 'Ho tro'),
('reply_consultant', 'Tra loi tu van', 'Ho tro'),
('manage_faqs', 'Quan ly FAQ', 'Ho tro'),
('manage_consultants', 'Quan ly nhan vien tu van', 'Nhan su'),
('view_driver_profiles', 'Xem ho so tai xe', 'Nhan su')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

-- SEED: ROLE -> PERMISSION MAPPING

INSERT INTO admin_role_permissions (role_id, permission_id, is_granted)
SELECT r.id, p.id, TRUE
FROM admin_roles r, admin_permissions p
WHERE r.role_key = 'owner'
ON DUPLICATE KEY UPDATE is_granted = TRUE;

INSERT INTO admin_role_permissions (role_id, permission_id, is_granted)
SELECT r.id, p.id, TRUE
FROM admin_roles r, admin_permissions p
WHERE r.role_key = 'admin' AND p.permission_key != 'manage_admins'
ON DUPLICATE KEY UPDATE is_granted = TRUE;

INSERT INTO admin_role_permissions (role_id, permission_id, is_granted)
SELECT r.id, p.id, TRUE
FROM admin_roles r, admin_permissions p
WHERE r.role_key = 'revenue_manager'
  AND p.permission_key IN ('dashboard', 'view_revenue', 'manage_payments', 'export_reports')
ON DUPLICATE KEY UPDATE is_granted = TRUE;

INSERT INTO admin_role_permissions (role_id, permission_id, is_granted)
SELECT r.id, p.id, TRUE
FROM admin_roles r, admin_permissions p
WHERE r.role_key = 'consultant'
  AND p.permission_key IN ('dashboard', 'view_consultant_chats', 'reply_consultant')
ON DUPLICATE KEY UPDATE is_granted = TRUE;

INSERT INTO admin_role_permissions (role_id, permission_id, is_granted)
SELECT r.id, p.id, TRUE
FROM admin_roles r, admin_permissions p
WHERE r.role_key = 'hr_manager'
  AND p.permission_key IN ('dashboard', 'manage_drivers', 'view_driver_profiles', 'manage_rides', 'view_rides')
ON DUPLICATE KEY UPDATE is_granted = TRUE;

-- SEED: ADMIN PANEL ACCOUNTS (password: Admin@123)

INSERT INTO admin_users (email, password, full_name, role_id, is_active)
SELECT 'owner@doan3.vn', '$2a$10$NQswCX7hQU/ruxwizDs6YeM7oc1WozDlBh9gsiulT3a18reIARI2W', 'Chu So Huu', r.id, TRUE
FROM admin_roles r WHERE r.role_key = 'owner'
ON DUPLICATE KEY UPDATE full_name = 'Chu So Huu';

INSERT INTO admin_users (email, password, full_name, role_id, is_active)
SELECT 'admin@doan3.vn', '$2a$10$NQswCX7hQU/ruxwizDs6YeM7oc1WozDlBh9gsiulT3a18reIARI2W', 'Quan Tri Vien', r.id, TRUE
FROM admin_roles r WHERE r.role_key = 'admin'
ON DUPLICATE KEY UPDATE full_name = 'Quan Tri Vien';

INSERT INTO admin_users (email, password, full_name, role_id, is_active)
SELECT 'tai_chinh@doan3.vn', '$2a$10$NQswCX7hQU/ruxwizDs6YeM7oc1WozDlBh9gsiulT3a18reIARI2W', 'Nguyen Van Tai', r.id, TRUE
FROM admin_roles r WHERE r.role_key = 'revenue_manager'
ON DUPLICATE KEY UPDATE full_name = 'Nguyen Van Tai';

INSERT INTO admin_users (email, password, full_name, role_id, is_active)
SELECT 'tu_van1@doan3.vn', '$2a$10$NQswCX7hQU/ruxwizDs6YeM7oc1WozDlBh9gsiulT3a18reIARI2W', 'Tran Thi Tu Van', r.id, TRUE
FROM admin_roles r WHERE r.role_key = 'consultant'
ON DUPLICATE KEY UPDATE full_name = 'Tran Thi Tu Van';

INSERT INTO admin_users (email, password, full_name, role_id, is_active)
SELECT 'hr@doan3.vn', '$2a$10$NQswCX7hQU/ruxwizDs6YeM7oc1WozDlBh9gsiulT3a18reIARI2W', 'Le Van Nhan Su', r.id, TRUE
FROM admin_roles r WHERE r.role_key = 'hr_manager'
ON DUPLICATE KEY UPDATE full_name = 'Le Van Nhan Su';
