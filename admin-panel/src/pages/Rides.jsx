import { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';
import {
  Search, ChevronLeft, ChevronRight, RefreshCw, Filter,
  Edit3, CheckCircle, XCircle, Clock, Car, AlertCircle
} from 'lucide-react';

function formatCurrency(num) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num || 0);
}

function formatDate(dateStr) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

const STATUS_COLORS = {
  pending: 'bg-yellow-100 text-yellow-700',
  accepted: 'bg-blue-100 text-blue-700',
  arrived: 'bg-indigo-100 text-indigo-700',
  in_progress: 'bg-purple-100 text-purple-700',
  completed: 'bg-green-100 text-green-700',
  cancelled: 'bg-red-100 text-red-700',
};

const STATUS_LABELS = {
  pending: 'Chờ nhận',
  accepted: 'Đã nhận',
  arrived: 'Đã đến',
  in_progress: 'Đang đi',
  completed: 'Hoàn thành',
  cancelled: 'Đã hủy',
};

const VEHICLE_LABELS = {
  motorbike: 'Xe máy',
  car_4_seats: 'Ô tô 4 chỗ',
  car_7_seats: 'Ô tô 7 chỗ',
};

export default function Rides() {
  const [rides, setRides] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, limit: 20, total: 0 });
  const [filters, setFilters] = useState({ status: 'all', from_date: '', to_date: '' });
  const [loading, setLoading] = useState(true);
  const [editingStatus, setEditingStatus] = useState(null);

  const fetchRides = async (page = 1) => {
    setLoading(true);
    try {
      const res = await adminAPI.rides({
        page,
        limit: pagination.limit,
        ...filters,
      });
      setRides(res.data.data);
      setPagination(prev => ({ ...prev, ...res.data.pagination }));
    } catch (err) {
      console.error('Failed to load rides:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRides();
  }, [filters.status]);

  const handleSearch = (e) => {
    e.preventDefault();
    setFilters(prev => ({ ...prev, from_date: e.target.fromDate?.value || '', to_date: e.target.toDate?.value || '' }));
    fetchRides(1);
  };

  const handleStatusFilter = (status) => {
    setFilters(prev => ({ ...prev, status }));
    fetchRides(1);
  };

  const updateRideStatus = async (rideId, newStatus) => {
    try {
      await adminAPI.updateRideStatus(rideId, newStatus);
      setRides(prev => prev.map(r => r.id === rideId ? { ...r, status: newStatus } : r));
      setEditingStatus(null);
    } catch (err) {
      alert('Cập nhật thất bại: ' + (err.response?.data?.message || err.message));
    }
  };

  const totalPages = Math.ceil(pagination.total / pagination.limit);

  const statusFilters = [
    { value: 'all', label: 'Tất cả' },
    { value: 'pending', label: 'Chờ nhận' },
    { value: 'accepted', label: 'Đã nhận' },
    { value: 'arrived', label: 'Đã đến' },
    { value: 'in_progress', label: 'Đang đi' },
    { value: 'completed', label: 'Hoàn thành' },
    { value: 'cancelled', label: 'Đã hủy' },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Chuyến đi</h1>
          <p className="text-sm text-gray-500 mt-0.5">{pagination.total} chuyến đi</p>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 mb-4 space-y-3">
        <div className="flex gap-2 flex-wrap">
          {statusFilters.map(({ value, label }) => (
            <button
              key={value}
              onClick={() => handleStatusFilter(value)}
              className={`px-3 py-1.5 text-sm rounded-lg border transition-colors ${
                filters.status === value
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50'
              }`}
            >
              {label}
            </button>
          ))}
        </div>
        <form onSubmit={handleSearch} className="flex flex-wrap gap-2 items-end">
          <div className="flex items-center gap-1.5">
            <span className="text-sm text-gray-500">Từ:</span>
            <input
              name="fromDate"
              type="date"
              className="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            />
          </div>
          <div className="flex items-center gap-1.5">
            <span className="text-sm text-gray-500">Đến:</span>
            <input
              name="toDate"
              type="date"
              className="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            />
          </div>
          <button
            type="submit"
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors"
          >
            Lọc
          </button>
        </form>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500 text-xs uppercase bg-gray-50">
                <th className="px-5 py-3">ID</th>
                <th className="px-5 py-3">Khách</th>
                <th className="px-5 py-3">Tài xế</th>
                <th className="px-5 py-3">Tuyến đường</th>
                <th className="px-5 py-3">Loại xe</th>
                <th className="px-5 py-3">Giá</th>
                <th className="px-5 py-3">Trạng thái</th>
                <th className="px-5 py-3">Thời gian</th>
                <th className="px-5 py-3">Hành động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {rides.map((ride) => (
                <tr key={ride.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-5 py-3 text-gray-500">#{ride.id}</td>
                  <td className="px-5 py-3 font-medium text-gray-800">{ride.passenger_name || '-'}</td>
                  <td className="px-5 py-3 text-gray-700">{ride.driver_name || '-'}</td>
                  <td className="px-5 py-3">
                    <p className="text-gray-600 max-w-xs truncate">{ride.pickup_address || '-'}</p>
                    {ride.dropoff_address && (
                      <p className="text-gray-400 text-xs max-w-xs truncate">→ {ride.dropoff_address}</p>
                    )}
                  </td>
                  <td className="px-5 py-3 text-gray-600 text-xs">
                    {VEHICLE_LABELS[ride.vehicle_type] || ride.vehicle_type || '-'}
                  </td>
                  <td className="px-5 py-3 font-medium text-gray-800">{formatCurrency(ride.price)}</td>
                  <td className="px-5 py-3">
                    {editingStatus === ride.id ? (
                      <select
                        autoFocus
                        defaultValue={ride.status}
                        onChange={(e) => updateRideStatus(ride.id, e.target.value)}
                        onBlur={() => setEditingStatus(null)}
                        className="px-2 py-1 border border-blue-300 rounded-lg text-xs focus:ring-2 focus:ring-blue-500 outline-none"
                      >
                        {Object.entries(STATUS_LABELS).map(([val, label]) => (
                          <option key={val} value={val}>{label}</option>
                        ))}
                      </select>
                    ) : (
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${STATUS_COLORS[ride.status] || 'bg-gray-100 text-gray-600'}`}>
                        {STATUS_LABELS[ride.status] || ride.status}
                      </span>
                    )}
                  </td>
                  <td className="px-5 py-3 text-gray-500 text-xs">{formatDate(ride.created_at)}</td>
                  <td className="px-5 py-3">
                    <button
                      onClick={() => setEditingStatus(ride.id)}
                      className="p-2 bg-blue-50 text-blue-600 hover:bg-blue-100 rounded-lg transition-colors"
                      title="Sửa trạng thái"
                    >
                      <Edit3 size={15} />
                    </button>
                  </td>
                </tr>
              ))}
              {!rides.length && !loading && (
                <tr>
                  <td colSpan={9} className="px-5 py-12 text-center text-gray-400">
                    Không tìm thấy chuyến đi nào
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
                onClick={() => fetchRides(pagination.page - 1)}
                disabled={pagination.page <= 1}
                className="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <ChevronLeft size={16} />
              </button>
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const p = pagination.page <= 3 ? i + 1 : pagination.page + i - 2;
                if (p < 1 || p > totalPages) return null;
                return (
                  <button
                    key={p}
                    onClick={() => fetchRides(p)}
                    className={`w-8 h-8 rounded-lg text-sm font-medium ${
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
                onClick={() => fetchRides(pagination.page + 1)}
                disabled={pagination.page >= totalPages}
                className="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed"
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
