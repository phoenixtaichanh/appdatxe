const adminAuthRepo = require('../repositories/adminAuthRepository');

/**
 * Middleware xac thuc admin (JWT session)
 * Doc token tu header Authorization: Bearer <token>
 */
async function adminAuth(req, res, next) {
    try {
        const authHeader = req.headers['authorization'];
        const token = authHeader && authHeader.split(' ')[1];

        if (!token) {
            return res.status(401).json({
                success: false,
                code: 'AUTH_REQUIRED',
                message: 'Vui long dang nhap de tiep tuc'
            });
        }

        const session = await adminAuthRepo.findSession(token);
        if (!session) {
            return res.status(401).json({
                success: false,
                code: 'SESSION_INVALID',
                message: 'Phien lam viec khong hop le hoac da het han'
            });
        }

        // Luu thong tin admin vao req
        req.admin = {
            id: session.admin_id,
            email: session.email,
            name: session.full_name,
            roleId: session.role_id,
            roleKey: session.role_key,
            roleName: session.role_name
        };

        next();
    } catch (error) {
        console.error('[AdminAuth] Error:', error.message);
        return res.status(500).json({
            success: false,
            code: 'SERVER_ERROR',
            message: 'Loi xac thuc admin'
        });
    }
}

/**
 * Middleware kiem tra quyen cu the
 * Su dung: requirePermission('manage_users')
 */
function requirePermission(permissionKey) {
    return async (req, res, next) => {
        try {
            // Owner luon co tat ca quyen
            if (req.admin.roleKey === 'owner') {
                return next();
            }

            const hasPermission = await adminAuthRepo.hasPermission(
                req.admin.id,
                permissionKey
            );

            if (!hasPermission) {
                return res.status(403).json({
                    success: false,
                    code: 'PERMISSION_DENIED',
                    message: `Ban khong co quyen: ${permissionKey}`
                });
            }

            next();
        } catch (error) {
            console.error('[RequirePermission] Error:', error.message);
            return res.status(500).json({
                success: false,
                code: 'SERVER_ERROR',
                message: 'Loi kiem tra quyen'
            });
        }
    };
}

/**
 * Middleware kiem tra cac quyen tuy chon
 * Su dung: requireAnyPermission(['view_revenue', 'manage_payments'])
 */
function requireAnyPermission(permissionKeys) {
    return async (req, res, next) => {
        try {
            if (req.admin.roleKey === 'owner') {
                return next();
            }

            const userPermissions = await adminAuthRepo.getUserPermissions(req.admin.id);

            const hasAny = permissionKeys.some(key => userPermissions.includes(key));
            if (!hasAny) {
                return res.status(403).json({
                    success: false,
                    code: 'PERMISSION_DENIED',
                    message: `Ban can mot trong cac quyen: ${permissionKeys.join(', ')}`
                });
            }

            next();
        } catch (error) {
            console.error('[RequireAnyPermission] Error:', error.message);
            return res.status(500).json({
                success: false,
                code: 'SERVER_ERROR',
                message: 'Loi kiem tra quyen'
            });
        }
    };
}

/**
 * Middleware chi cho phep owner
 */
function ownerOnly(req, res, next) {
    if (req.admin.roleKey !== 'owner') {
        return res.status(403).json({
            success: false,
            code: 'OWNER_ONLY',
            message: 'Chuc nang chi danh cho chu so huu'
        });
    }
    next();
}

/**
 * Tao token ngau nhien
 */
function generateToken() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let token = '';
    for (let i = 0; i < 64; i++) {
        token += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return token;
}

module.exports = {
    adminAuth,
    requirePermission,
    requireAnyPermission,
    ownerOnly,
    generateToken
};
