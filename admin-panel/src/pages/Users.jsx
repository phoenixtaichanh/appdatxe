import { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';
import {
  Search, ChevronLeft, ChevronRight, UserX, UserCheck,
  AlertCircle, CheckCircle, Ban, RefreshCw, UserCog
} from 'lucide-react';

const USER_TYPE_COLORS = {
  owner: 'bg-purple-100 text-purple-700',
  admin: 'bg-red-100 text-red-700',
  revenue_manager: 'bg-orange-100 text-orange-700',
  driver: 'bg-blue-100 text-blue-700',
  passenger: 'bg-green-100 text-green-700',
};

const USER_TYPE_LABELS = {
  owner: 'Chủ sở hữu',
  admin: 'Quản trị',
  revenue_manager: 'Quản lý DT',
  driver: 'Tài xế',
  passenger: 'Khách',
};

function formatDate(dateStr) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

export default function Users() {
  const [users, setUsers] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, limit: 20, total: 0 });
  const [filters, setFilters] = useState({ role: 'all', search: '' });
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);

  const fetchUsers = async (page = 1) => {
    setLoading(true);
    try {
      const res = await adminAPI.users({
        page,
        limit: pagination.limit,
        role: filters.role,
        search: filters.search,
      });
      setUsers(res.data.data);
      setPagination(prev => ({ ...prev, ...res.data.pagination }));
    } catch (err) {
      console.error('Failed to load users:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [filters.role]);

  const handleSearch = (e) => {
    e.preventDefault();
    setFilters(prev => ({ ...prev, search: e.target.searchInput.value }));
    fetchUsers(1);
  };

  const handleRoleFilter = (role) => {
    setFilters(prev => ({ ...prev, role }));
    fetchUsers(1);
  };

  const toggleUserStatus = async (user) => {
    if (!confirm(`Bạn muốn ${user.is_active ? 'vô hiệu hóa' : 'kích hoạt'} tài khoản "${user.name}"?`)) return;
    setActionLoading(user.id);
    try {
      await adminAPI.updateUserStatus(user.id, !user.is_active);
      setUsers(prev => prev.map(u => u.id === user.id ? { ...u, is_active: !u.is_active } : u));
    } catch (err) {
      alert('Cập nhật thất bại: ' + (err.response?.data?.message || err.message));
    } finally {
      setActionLoading(null);
    }
  };

  const totalPages = Math.ceil(pagination.total / pagination.limit);

  const roleFilters = [
    { value: 'all', label: 'Tất cả' },
    { value: 'passenger', label: 'Khách' },
    { value: 'driver', label: 'Tài xế' },
    { value: 'admin', label: 'Admin' },
    { value: 'owner', label: 'Chủ sở hữu' },
    { value: 'revenue_manager', label: 'QL Doanh thu' },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Người dùng</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            {pagination.total} người dùng
          </p>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 mb-4">
        <div className="p-4 flex flex-col sm:flex-row gap-3">
          <form onSubmit={handleSearch} className="flex-1 flex gap-2">
            <div className="relative flex-1">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                name="searchInput"
                type="text"
                placeholder="Tìm theo tên, email, số điện thoại..."
                className="w-full pl-9 pr-4 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
              />
            </div>
            <button
              type="submit"
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors"
            >
              Tìm kiếm
            </button>
          </form>
          <div className="flex gap-2 flex-wrap">
            {roleFilters.map(({ value, label }) => (
              <button
                key={value}
                onClick={() => handleRoleFilter(value)}
                className={`px-3 py-1.5 text-sm rounded-lg border transition-colors ${
                  filters.role === value
                    ? 'bg-blue-600 text-white border-blue-600'
                    : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50'
                }`}
              >
                {label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500 text-xs uppercase bg-gray-50">
                <th className="px-5 py-3">ID</th>
                <th className="px-5 py-3">Thông tin</th>
                <th className="px-5 py-3">Loại</th>
                <th className="px-5 py-3">Đánh giá</th>
                <th className="px-5 py-3">Chuyến đi</th>
                <th className="px-5 py-3">Trạng thái</th>
                <th className="px-5 py-3">Ngày tạo</th>
                <th className="px-5 py-3">Hành động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {users.map((user) => (
                <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-5 py-3 text-gray-500">#{user.id}</td>
                  <td className="px-5 py-3">
                    <p className="font-medium text-gray-800">{user.name || '-'}</p>
                    <p className="text-xs text-gray-500">{user.email}</p>
                    <p className="text-xs text-gray-400">{user.phone || ''}</p>
                  </td>
                  <td className="px-5 py-3">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${USER_TYPE_COLORS[user.user_type] || 'bg-gray-100 text-gray-600'}`}>
                      {USER_TYPE_LABELS[user.user_type] || user.user_type}
                    </span>
                  </td>
                  <td className="px-5 py-3">
                    {user.rating ? (
                      <span className="text-yellow-500 font-medium">★ {Number(user.rating).toFixed(1)}</span>
                    ) : (
                      <span className="text-gray-400">-</span>
                    )}
                  </td>
                  <td className="px-5 py-3 text-gray-700">{user.total_rides || 0}</td>
                  <td className="px-5 py-3">
                    {user.is_active ? (
                      <span className="flex items-center gap-1 text-green-600 text-xs font-medium">
                        <CheckCircle size={14} /> Hoạt động
                      </span>
                    ) : (
                      <span className="flex items-center gap-1 text-red-600 text-xs font-medium">
                        <Ban size={14} /> Bị khóa
                      </span>
                    )}
                  </td>
                  <td className="px-5 py-3 text-gray-500 text-xs">{formatDate(user.created_at)}</td>
                  <td className="px-5 py-3">
                    <button
                      onClick={() => toggleUserStatus(user)}
                      disabled={actionLoading === user.id}
                      className={`p-2 rounded-lg text-sm transition-colors disabled:opacity-50 ${
                        user.is_active
                          ? 'bg-red-50 text-red-600 hover:bg-red-100'
                          : 'bg-green-50 text-green-600 hover:bg-green-100'
                      }`}
                      title={user.is_active ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}
                    >
                      {actionLoading === user.id ? (
                        <RefreshCw size={15} className="animate-spin" />
                      ) : user.is_active ? (
                        <UserX size={15} />
                      ) : (
                        <UserCheck size={15} />
                      )}
                    </button>
                  </td>
                </tr>
              ))}
              {!users.length && !loading && (
                <tr>
                  <td colSpan={8} className="px-5 py-12 text-center text-gray-400">
                    Không tìm thấy người dùng nào
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-5 py-3 border-t border-gray-100">
            <p className="text-sm text-gray-500">
              Trang {pagination.page} / {totalPages} — {pagination.total} kết quả
            </p>
            <div className="flex gap-1">
              <button
                onClick={() => fetchUsers(pagination.page - 1)}
                disabled={pagination.page <= 1}
                className="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              >
                <ChevronLeft size={16} />
              </button>
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const p = pagination.page <= 3 ? i + 1 : pagination.page + i - 2;
                if (p < 1 || p > totalPages) return null;
                return (
                  <button
                    key={p}
                    onClick={() => fetchUsers(p)}
                    className={`w-8 h-8 rounded-lg text-sm font-medium transition-colors ${
                      pagination.page === p
                        ? 'bg-blue-600 text-white'
                        : 'border border-gray-200 text-gray-600 hover:bg-gray-50'
                    }`}
                  >
                    {p}
                  </button>
                );
              })}
              <button
                onClick={() => fetchUsers(pagination.page + 1)}
                disabled={pagination.page >= totalPages}
                className="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              >
                <ChevronRight size={16} />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
