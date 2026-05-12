-- =====================================================
-- DoAn3 Seed Data
-- Tài khoản test và dữ liệu mẫu
-- =====================================================

USE doan3_db;

-- =====================================================
-- TEST USERS
-- Password for all: password123
-- Generated hash: $2a$10$O4ybrThFykUGyJug0Yar.uSEMlYPqPS8TriozRsmiVmpzI4ngeTc.
-- =====================================================

-- Passenger test account
INSERT INTO users (email, password, name, phone, user_type, rating, total_rides)
VALUES (
    'passenger@test.com',
    '$2a$10$O4ybrThFykUGyJug0Yar.uSEMlYPqPS8TriozRsmiVmpzI4ngeTc.',
    'Nguyễn Văn Test',
    '0909123456',
    'passenger',
    5.00,
    0
);

-- Driver 1 test account
INSERT INTO users (email, password, name, phone, user_type, rating, total_rides)
VALUES (
    'driver1@test.com',
    '$2a$10$O4ybrThFykUGyJug0Yar.uSEMlYPqPS8TriozRsmiVmpzI4ngeTc.',
    'Trần Văn Tài 1',
    '0909234567',
    'driver',
    4.80,
    42
);

-- Driver 2 test account
INSERT INTO users (email, password, name, phone, user_type, rating, total_rides)
VALUES (
    'driver2@test.com',
    '$2a$10$O4ybrThFykUGyJug0Yar.uSEMlYPqPS8TriozRsmiVmpzI4ngeTc.',
    'Lê Thị Xe 2',
    '0909345678',
    'driver',
    4.65,
    28
);

-- Driver 3 test account
INSERT INTO users (email, password, name, phone, user_type, rating, total_rides)
VALUES (
    'driver3@test.com',
    '$2a$10$O4ybrThFykUGyJug0Yar.uSEMlYPqPS8TriozRsmiVmpzI4ngeTc.',
    'Phạm Xuân Lái 3',
    '0909456789',
    'driver',
    4.92,
    65
);

-- =====================================================
-- DRIVER PROFILES
-- =====================================================

INSERT INTO drivers (user_id, car_model, car_color, license_plate, is_available, latitude, longitude)
VALUES
    (2, 'Toyota Camry', 'Đen', '43A-123.45', TRUE, 16.0544, 108.2022),
    (3, 'Honda Vision', 'Trắng', '43L-567.89', TRUE, 16.0600, 108.2100),
    (4, 'Ford Everest', 'Bạc', '43K-999.99', TRUE, 16.0700, 108.2200);

-- =====================================================
-- SAMPLE RIDES
-- =====================================================

INSERT INTO rides (passenger_id, driver_id, pickup_lat, pickup_lng, pickup_address,
    dest_lat, dest_lng, dest_address, distance_km, duration_min, price, status,
    driver_rating, created_at)
VALUES
    -- Completed rides
    (1, 2, 16.0544, 108.2022, 'Sân bay Đà Nẵng',
        16.0678, 108.2100, 'Trường ĐH Bách Khoa Đà Nẵng',
        2.5, 8, 24000, 'completed',
        5, DATE_SUB(NOW(), INTERVAL 3 DAY)),

    (1, 3, 16.0544, 108.2022, 'Sân bay Đà Nẵng',
        16.0800, 108.2300, 'Bãi Biển Mỹ Khê',
        4.2, 12, 31000, 'completed',
        4, DATE_SUB(NOW(), INTERVAL 1 DAY)),

    (1, NULL, 16.0600, 108.2100, 'Trường ĐH Bách Khoa',
        16.0544, 108.2022, 'Sân bay Đà Nẵng',
        1.8, 6, 19800, 'pending', NULL, NOW());

-- =====================================================
-- DRIVER LOCATIONS (cho nearby drivers)
-- =====================================================

INSERT INTO driver_locations (driver_id, latitude, longitude, updated_at)
VALUES
    (2, 16.0545, 108.2023, NOW()),
    (3, 16.0600, 108.2100, NOW()),
    (4, 16.0700, 108.2200, NOW());

-- =====================================================
-- AI LEARNING PROFILES
-- =====================================================

INSERT INTO ai_learning_profiles (user_id, preferred_time_start, preferred_time_end,
    average_trip_duration, average_trip_cost, total_distance_travelled,
    preference_cost_vs_time, model_version)
VALUES
    (1, '08:00:00', '20:00:00', 25.5, 65000, 450.5, 0.60, 'v1.1'),
    (2, '06:00:00', '22:00:00', 0, 0, 0, 0.50, 'v1.0'),
    (3, '06:00:00', '22:00:00', 0, 0, 0, 0.50, 'v1.0'),
    (4, '06:00:00', '22:00:00', 0, 0, 0, 0.50, 'v1.0');

-- =====================================================
-- EARNINGS (cho driver)
-- =====================================================

INSERT INTO earnings (driver_id, ride_id, amount, type, created_at)
VALUES
    (2, 1, 24000, 'ride', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (3, 2, 31000, 'ride', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- AI TRIP SCHEDULES (sample)
-- =====================================================

INSERT INTO ai_trip_schedules (user_id, schedule_name, scheduled_date,
    total_estimated_time, total_estimated_price, total_distance,
    optimization_type, ai_confidence_score, traffic_condition, status)
VALUES
    (1, 'Du lịch Đà Nẵng 1 ngày', DATE_ADD(CURDATE(), INTERVAL 7 DAY),
        480, 350000, 45.5, 'balanced', 0.92, 'typical', 'planned');

-- Get the schedule ID
SET @schedule_id = LAST_INSERT_ID();

INSERT INTO ai_waypoints (schedule_id, stop_order, stop_type, latitude, longitude,
    address, stop_name, distance_from_prev, duration_min, estimated_price_segment)
VALUES
    (@schedule_id, 1, 'pickup', 16.0544, 108.2022, 'Sân bay Đà Nẵng', 'Sân bay', 0, 0, 0),
    (@schedule_id, 2, 'stopover', 16.0678, 108.2100, 'Bãi Biển Mỹ Khê', 'Mỹ Khê', 2.5, 30, 22500),
    (@schedule_id, 3, 'stopover', 15.9802, 108.2677, 'Phố cổ Hội An', 'Hội An', 15.2, 60, 86000),
    (@schedule_id, 4, 'dropoff', 16.0544, 108.2022, 'Sân bay Đà Nẵng', 'Sân bay', 17.8, 45, 241500);

INSERT INTO ai_route_alternatives (schedule_id, route_name, total_distance,
    total_duration, total_price, route_description, is_recommended, traffic_scenario, weather_impact)
VALUES
    (@schedule_id, 'Nhanh nhat', 40.0, 360, 320000,
        'Tuyen duong nhanh nhat, co the di qua duong cao toc hoac duong tat',
        FALSE, 'morning_peak', 0.1),
    (@schedule_id, 'Re nhat', 48.0, 420, 295000,
        'Tuyen duong tiet kiem chi phi nhat, chon duong tran cao toc',
        FALSE, 'typical', 0.0),
    (@schedule_id, 'Can bang', 45.5, 390, 350000,
        'Tuyen duong can bang giua thoi gian va chi phi',
        TRUE, 'typical', 0.0);
