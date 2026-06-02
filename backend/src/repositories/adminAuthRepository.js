const { pool } = require('../database/db');

class AdminAuthRepository {
    // -------- Tim admin user theo email --------
    async findByEmail(email) {
        const [rows] = await pool.query(`
            SELECT au.*, ar.role_key, ar.role_name
            FROM admin_users au
            JOIN admin_roles ar ON au.role_id = ar.id
            WHERE au.email = ?
        `, [email]);
        return rows[0] || null;
    }

    // -------- Tim admin user theo ID --------
    async findById(id) {
        const [rows] = await pool.query(`
            SELECT au.id, au.email, au.full_name, au.phone, au.avatar_url,
                   au.role_id, au.is_active, au.created_at,
                   ar.role_key, ar.role_name
            FROM admin_users au
            JOIN admin_roles ar ON au.role_id = ar.id
            WHERE au.id = ?
        `, [id]);
        return rows[0] || null;
    }

    // -------- Lay tat ca quyen cua mot role --------
    async getPermissionsByRoleId(roleId) {
        const [rows] = await pool.query(`
            SELECT p.permission_key, p.permission_name, p.group_name
            FROM admin_role_permissions rp
            JOIN admin_permissions p ON rp.permission_id = p.id
            WHERE rp.role_id = ? AND rp.is_granted = TRUE
        `, [roleId]);
        return rows;
    }

    // -------- Lay tat ca quyen cua mot admin user --------
    async getUserPermissions(adminUserId) {
        const [rows] = await pool.query(`
            SELECT p.permission_key
            FROM admin_users au
            JOIN admin_role_permissions rp ON au.role_id = rp.role_id
            JOIN admin_permissions p ON rp.permission_id = p.id
            WHERE au.id = ? AND rp.is_granted = TRUE AND au.is_active = TRUE
        `, [adminUserId]);
        return rows.map(r => r.permission_key);
    }

    // -------- Cap nhat last login --------
    async updateLastLogin(id, ip) {
        await pool.query(`
            UPDATE admin_users SET last_login_at = NOW(), last_login_ip = ? WHERE id = ?
        `, [ip, id]);
    }

    // -------- Tao session --------
    async createSession(adminUserId, token, expiresAt, ip, userAgent) {
        await pool.query(`
            INSERT INTO admin_sessions (admin_user_id, token, expires_at, ip_address, user_agent)
            VALUES (?, ?, ?, ?, ?)
        `, [adminUserId, token, expiresAt, ip, userAgent]);
    }

    // -------- Tim session theo token --------
    async findSession(token) {
        const [rows] = await pool.query(`
            SELECT s.*, au.id as admin_id, au.email, au.full_name, au.is_active as admin_active,
                   ar.role_key, ar.role_name, ar.id as role_id
            FROM admin_sessions s
            JOIN admin_users au ON s.admin_user_id = au.id
            JOIN admin_roles ar ON au.role_id = ar.id
            WHERE s.token = ? AND s.expires_at > NOW() AND au.is_active = TRUE
        `, [token]);
        return rows[0] || null;
    }

    // -------- Xoa session (logout) --------
    async deleteSession(token) {
        await pool.query('DELETE FROM admin_sessions WHERE token = ?', [token]);
    }

    // -------- Xoa tat ca session cua mot user --------
    async deleteAllUserSessions(adminUserId) {
        await pool.query('DELETE FROM admin_sessions WHERE admin_user_id = ?', [adminUserId]);
    }

    // -------- Don dep session het han --------
    async cleanExpiredSessions() {
        await pool.query('DELETE FROM admin_sessions WHERE expires_at < NOW()');
    }

    // -------- Kiem tra quyen --------
    async hasPermission(adminUserId, permissionKey) {
        const [rows] = await pool.query(`
            SELECT 1 FROM admin_users au
            JOIN admin_role_permissions rp ON au.role_id = rp.role_id
            JOIN admin_permissions p ON rp.permission_id = p.id
            WHERE au.id = ?
              AND p.permission_key = ?
              AND rp.is_granted = TRUE
              AND au.is_active = TRUE
            LIMIT 1
        `, [adminUserId, permissionKey]);
        return rows.length > 0;
    }

    // -------- CRUD: Lay danh sach admin --------
    async findAll(page = 1, limit = 20, search = '', roleKey = '') {
        const offset = (page - 1) * limit;
        let sql = `
            SELECT au.id, au.email, au.full_name, au.phone, au.avatar_url,
                   au.is_active, au.last_login_at, au.last_login_ip, au.created_at,
                   ar.id as role_id, ar.role_key, ar.role_name
            FROM admin_users au
            JOIN admin_roles ar ON au.role_id = ar.id
            WHERE 1=1
        `;
        const params = [];

        if (search) {
            sql += ' AND (au.full_name LIKE ? OR au.email LIKE ?)';
            params.push(`%${search}%`, `%${search}%`);
        }
        if (roleKey) {
            sql += ' AND ar.role_key = ?';
            params.push(roleKey);
        }

        const [countRows] = await pool.query(
            sql.replace('SELECT au.id, au.email, au.full_name, au.phone, au.avatar_url,\n                   au.is_active, au.last_login_at, au.last_login_ip, au.created_at,\n                   ar.id as role_id, ar.role_key, ar.role_name',
                'SELECT COUNT(*) as total'),
            params
        );
        const total = countRows[0].total;

        sql += ' ORDER BY au.created_at DESC LIMIT ? OFFSET ?';
        params.push(limit, offset);
        const [rows] = await pool.query(sql, params);

        return { data: rows, total, page, limit, totalPages: Math.ceil(total / limit) };
    }

    // -------- CRUD: Tao admin --------
    async create({ email, passwordHash, fullName, phone, roleId, createdBy }) {
        const [result] = await pool.query(`
            INSERT INTO admin_users (email, password, full_name, phone, role_id, created_by)
            VALUES (?, ?, ?, ?, ?, ?)
        `, [email, passwordHash, fullName, phone || null, roleId, createdBy || null]);
        return result.insertId;
    }

    // -------- CRUD: Cap nhat admin --------
    async update(id, { fullName, phone, roleId, isActive }) {
        const fields = [];
        const params = [];
        if (fullName !== undefined) { fields.push('full_name = ?'); params.push(fullName); }
        if (phone !== undefined) { fields.push('phone = ?'); params.push(phone); }
        if (roleId !== undefined) { fields.push('role_id = ?'); params.push(roleId); }
        if (isActive !== undefined) { fields.push('is_active = ?'); params.push(isActive); }
        if (!fields.length) return;

        params.push(id);
        await pool.query(`UPDATE admin_users SET ${fields.join(', ')} WHERE id = ?`, params);
    }

    // -------- CRUD: Doi mat khau --------
    async updatePassword(id, passwordHash) {
        await pool.query('UPDATE admin_users SET password = ? WHERE id = ?', [passwordHash, id]);
    }

    // -------- CRUD: Xoa admin --------
    async delete(id) {
        // Khong cho xoa tai khoan owner cuoi cung
        const [rows] = await pool.query(`
            SELECT ar.role_key FROM admin_users au
            JOIN admin_roles ar ON au.role_id = ar.id
            WHERE au.id = ? AND au.is_active = TRUE
        `, [id]);
        if (rows[0]?.role_key === 'owner') {
            const [count] = await pool.query(
                'SELECT COUNT(*) as total FROM admin_users au JOIN admin_roles ar ON au.role_id = ar.id WHERE ar.role_key = ? AND au.is_active = TRUE',
                ['owner']
            );
            if (count[0].total <= 1) {
                throw new Error('Khong the xoa tai khoan owner cuoi cung');
            }
        }
        // Soft delete
        await pool.query('UPDATE admin_users SET is_active = FALSE WHERE id = ?', [id]);
    }

    // -------- Lay danh sach role --------
    async getAllRoles() {
        const [rows] = await pool.query('SELECT * FROM admin_roles WHERE is_active = TRUE ORDER BY id');
        return rows;
    }

    // -------- Lay danh sach quyen --------
    async getAllPermissions() {
        const [rows] = await pool.query(`
            SELECT p.*, r.role_key
            FROM admin_permissions p
            JOIN admin_role_permissions rp ON p.id = rp.permission_id
            JOIN admin_roles r ON rp.role_id = r.id
            ORDER BY p.group_name, p.permission_name
        `);
        return rows;
    }

    // -------- Lay role theo key --------
    async findRoleByKey(roleKey) {
        const [rows] = await pool.query('SELECT * FROM admin_roles WHERE role_key = ?', [roleKey]);
        return rows[0] || null;
    }
}

module.exports = new AdminAuthRepository();
