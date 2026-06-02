/**
 * Admin Auth Routes
 * Dang nhap, dang xuat, quan ly tai khoan admin
 */
const express = require('express');
const bcrypt = require('bcryptjs');
const { adminAuth, requirePermission, ownerOnly, generateToken } = require('../middleware/adminAuth');
const adminAuthRepo = require('../repositories/adminAuthRepository');

const router = express.Router();

const SALT_ROUNDS = 10;
const SESSION_EXPIRY_HOURS = 24;

// ============================================================
// PUBLIC ROUTES (khong can dang nhap)
// ============================================================

/**
 * POST /api/admin-auth/login
 * Dang nhap admin panel
 */
router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({
                success: false,
                message: 'Email va mat khau la bat buoc'
            });
        }

        const admin = await adminAuthRepo.findByEmail(email);

        if (!admin || !admin.is_active) {
            return res.status(401).json({
                success: false,
                message: 'Email hoac mat khau khong dung'
            });
        }

        const isPasswordValid = await bcrypt.compare(password, admin.password);
        if (!isPasswordValid) {
            return res.status(401).json({
                success: false,
                message: 'Email hoac mat khau khong dung'
            });
        }

        // Tao session token
        const token = generateToken();
        const expiresAt = new Date(Date.now() + SESSION_EXPIRY_HOURS * 60 * 60 * 1000);

        await adminAuthRepo.createSession(
            admin.id,
            token,
            expiresAt,
            req.ip,
            req.headers['user-agent']
        );
        await adminAuthRepo.updateLastLogin(admin.id, req.ip);

        // Lay danh sach quyen
        const permissions = await adminAuthRepo.getPermissionsByRoleId(admin.role_id);

        res.json({
            message: 'Dang nhap thanh cong',
            data: {
                token,
                expiresAt: expiresAt.toISOString(),
                admin: {
                    id: admin.id,
                    email: admin.email,
                    fullName: admin.full_name,
                    phone: admin.phone,
                    avatarUrl: admin.avatar_url,
                    role: {
                        id: admin.role_id,
                        key: admin.role_key,
                        name: admin.role_name
                    }
                },
                permissions: permissions.map(p => ({
                    key: p.permission_key,
                    name: p.permission_name,
                    group: p.group_name
                }))
            }
        });
    } catch (error) {
        console.error('[AdminAuth/Login] Error:', error);
        res.status(500).json({ success: false, message: 'Loi server' });
    }
});

/**
 * POST /api/admin-auth/logout
 * Dang xuat
 */
router.post('/logout', adminAuth, async (req, res) => {
    try {
        const token = req.headers['authorization']?.split(' ')[1];
        if (token) {
            await adminAuthRepo.deleteSession(token);
        }
        res.json({ message: 'Dang xuat thanh cong' });
    } catch (error) {
        console.error('[AdminAuth/Logout] Error:', error);
        res.status(500).json({ success: false, message: 'Loi server' });
    }
});

// ============================================================
// PROTECTED ROUTES (can dang nhap)
// ============================================================

/**
 * GET /api/admin-auth/me
 * Lay thong tin admin hien tai
 */
router.get('/me', adminAuth, async (req, res) => {
    try {
        const admin = await adminAuthRepo.findById(req.admin.id);
        if (!admin) {
            return res.status(404).json({ success: false, message: 'Tai khoan khong ton tai' });
        }

        const permissions = await adminAuthRepo.getPermissionsByRoleId(admin.role_id);

        res.json({
            data: {
                id: admin.id,
                email: admin.email,
                fullName: admin.full_name,
                phone: admin.phone,
                avatarUrl: admin.avatar_url,
                lastLoginAt: admin.last_login_at,
                createdAt: admin.created_at,
                role: {
                    id: admin.role_id,
                    key: admin.role_key,
                    name: admin.role_name
                },
                permissions: permissions.map(p => ({
                    key: p.permission_key,
                    name: p.permission_name,
                    group: p.group_name
                }))
            }
        });
    } catch (error) {
        console.error('[AdminAuth/Me] Error:', error);
        res.status(500).json({ success: false, message: 'Loi server' });
    }
});

/**
 * GET /api/admin-auth/admins
 * Lay danh sach tai khoan admin (phan trang)
 * Chi owner hoac admin moi co quyen xem
 */
router.get('/admins', adminAuth, requirePermission('manage_admins'), async (req, res) => {
    try {
        const { page = 1, limit = 20, search = '', role = '' } = req.query;

        // Revenue manager va consultant khong duoc xem danh sach admin
        if (['revenue_manager', 'consultant'].includes(req.admin.roleKey)) {
            return res.status(403).json({
                success: false,
                message: 'Ban khong co quyen xem danh sach tai khoan admin'
            });
        }

        // Owner: thay tat ca
        // Admin: khong thay owner
        const roleFilter = role;
        const searchFilter = search;

        const result = await adminAuthRepo.findAll(
            parseInt(page),
            parseInt(limit),
            searchFilter,
            roleFilter
        );

        // Loc bot tai khoan owner neu la admin
        if (req.admin.roleKey === 'admin') {
            result.data = result.data.filter(a => a.role_key !== 'owner');
        }

        res.json(result);
    } catch (error) {
        console.error('[AdminAuth/Admins] Error:', error);
        res.status(500).json({ success: false, message: 'Loi server' });
    }
});

/**
 * POST /api/admin-auth/admins
 * Tao tai khoan admin moi
 * Chi owner moi co quyen tao admin
 */
router.post('/admins', adminAuth, ownerOnly, async (req, res) => {
    try {
        const { email, password, fullName, phone, roleId } = req.body;

        if (!email || !password || !fullName || !roleId) {
            return res.status(400).json({
                success: false,
                message: 'Email, mat khau, ten, vai tro la bat buoc'
            });
        }

        // Kiem tra email da ton tai
        const existing = await adminAuthRepo.findByEmail(email);
        if (existing) {
            return res.status(409).json({
                success: false,
                message: 'Email da ton tai'
            });
        }

        // Hash mat khau
        const passwordHash = await bcrypt.hash(password, SALT_ROUNDS);

        // Tao tai khoan
        const id = await adminAuthRepo.create({
            email,
            passwordHash,
            fullName,
            phone,
            roleId: parseInt(roleId),
            createdBy: req.admin.id
        });

        const newAdmin = await adminAuthRepo.findById(id);

        res.status(201).json({
            message: 'Tao tai khoan thanh cong',
            data: {
                id: newAdmin.id,
                email: newAdmin.email,
                fullName: newAdmin.full_name,
                phone: newAdmin.phone,
                isActive: newAdmin.is_active,
                role: {
                    id: newAdmin.role_id,
                    key: newAdmin.role_key,
                    name: newAdmin.role_name
                },
                createdAt: newAdmin.created_at
            }
        });
    } catch (error) {
        console.error('[AdminAuth/CreateAdmin] Error:', error);
        res.status(500).json({ success: false, message: 'Loi server' });
    }
});

/**
 * PUT /api/admin-auth/admins/:id
 * Cap nhat tai khoan admin
 * Owner: co the sua tat ca
 * Admin: khong the sua owner, chi sua nhan vien khac
 */
router.put('/admins/:id', adminAuth, async (req, res) => {
    try {
        const adminId = parseInt(req.params.id);
        const { fullName, phone, roleId, isActive } = req.body;

        // Tim tai khoan can sua
        const target = await adminAuthRepo.findById(adminId);
        if (!target) {
            return res.status(404).json({ success: false, message: 'Tai khoan khong ton tai' });
        }

        // Quyen chinh sua
        if (req.admin.roleKey === 'admin') {
            // Admin khong the sua owner
            if (target.role_key === 'owner') {
                return res.status(403).json({
                    success: false,
                    message: 'Ban khong co quyen sua tai khoan chu so huu'
                });
            }
            // Admin chi duoc sua nhan vien (khong phai admin khac)
            if (target.role_key === 'admin') {
                return res.status(403).json({
                    success: false,
                    message: 'Ban khong co quyen sua tai khoan quan tri vien khac'
                });
            }
        }

        // Khong cho doi vai tro owner
        if (target.role_key === 'owner' && roleId && roleId !== target.role_id) {
            return res.status(403).json({
                success: false,
                message: 'Khong the thay doi vai tro cua chu so huu'
            });
        }

        await adminAuthRepo.update(adminId, {
            fullName,
            phone,
            roleId: roleId ? parseInt(roleId) : undefined,
            isActive
        });

        const updated = await adminAuthRepo.findById(adminId);

        res.json({
            message: 'Cap nhat thanh cong',
            data: {
                id: updated.id,
                email: updated.email,
                fullName: updated.full_name,
                phone: updated.phone,
                isActive: updated.is_active,
                role: {
                    id: updated.role_id,
                    key: updated.role_key,
                    name: updated.role_name
                }
            }
        });
    } catch (error) {
        console.error('[AdminAuth/UpdateAdmin] Error:', error);
        res.status(500).json({ success: false, message: 'Loi server' });
    }
});

/**
 * PUT /api/admin-auth/admins/:id/password
 * Doi mat khau admin
 */
router.put('/admins/:id/password', adminAuth, async (req, res) => {
    try {
        const adminId = parseInt(req.params.id);
        const { newPassword } = req.body;

        if (!newPassword || newPassword.length < 6) {
            return res.status(400).json({
                success: false,
                message: 'Mat khau moi phai it nhat 6 ky tu'
            });
        }

        const target = await adminAuthRepo.findById(adminId);
        if (!target) {
            return res.status(404).json({ success: false, message: 'Tai khoan khong ton tai' });
        }

        // Quyen doi mat khau
        const canChange = req.admin.roleKey === 'owner' ||
                          req.admin.id === adminId;

        if (!canChange) {
            return res.status(403).json({
                success: false,
                message: 'Ban khong co quyen doi mat khau tai khoan nay'
            });
        }

        const passwordHash = await bcrypt.hash(newPassword, SALT_ROUNDS);
        await adminAuthRepo.updatePassword(adminId, passwordHash);

        // Neu doi mat khau cua minh, tao session moi
        if (req.admin.id === adminId) {
            // Xoa tat ca session cu
            await adminAuthRepo.deleteAllUserSessions(adminId);
        }

        res.json({ message: 'Doi mat khau thanh cong' });
    } catch (error) {
        console.error('[AdminAuth/ChangePassword] Error:', error);
        res.status(500).json({ success: false, message: 'Loi server' });
    }
});

/**
 * DELETE /api/admin-auth/admins/:id
 * Xoa (vo hieu hoa) tai khoan admin
 * Chi owner moi co quyen xoa
 */
router.delete('/admins/:id', adminAuth, ownerOnly, async (req, res) => {
    try {
        const adminId = parseInt(req.params.id);

        if (req.admin.id === adminId) {
            return res.status(400).json({
                success: false,
                message: 'Khong the xoa tai khoan cua chinh minh'
            });
        }

        await adminAuthRepo.delete(adminId);

        res.json({ message: 'Xoa tai khoan thanh cong' });
    } catch (error) {
        console.error('[AdminAuth/DeleteAdmin] Error:', error);
        if (error.message.includes('cuoi cung')) {
            return res.status(400).json({ success: false, message: error.message });
        }
        res.status(500).json({ success: false, message: 'Loi server' });
    }
});

/**
 * GET /api/admin-auth/roles
 * Lay danh sach vai tro (chi owner/admin)
 */
router.get('/roles', adminAuth, requirePermission('manage_admins'), async (req, res) => {
    try {
        const roles = await adminAuthRepo.getAllRoles();

        // Admin khong thay role owner
        if (req.admin.roleKey === 'admin') {
            return res.json({ data: roles.filter(r => r.role_key !== 'owner') });
        }

        res.json({ data: roles });
    } catch (error) {
        console.error('[AdminAuth/Roles] Error:', error);
        res.status(500).json({ success: false, message: 'Loi server' });
    }
});

module.exports = router;
