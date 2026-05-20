import { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';
import { Search, ChevronLeft, ChevronRight, RefreshCw, Star, Car, MapPin } from 'lucide-react';

function formatCurrency(num) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num || 0);
}

function formatDate(dateStr) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
  });
}

export default function Drivers() {
  const [drivers, setDrivers] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, limit: 20, total: 0 });
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchDrivers = async (page = 1, searchTerm = search) => {
    setLoading(true);
    try {
      const res = await adminAPI.drivers({
        page,
        limit: pagination.limit,
        search: searchTerm,
      });
      setDrivers(res.data.data);
      setPagination(prev => ({ ...prev, ...res.data.pagination }));
    } catch (err) {
      console.error('Failed to load drivers:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDrivers();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    setSearch(e.target.searchInput.value);
    fetchDrivers(1, e.target.searchInput.value);
  };

  const totalPages = Math.ceil(pagination.total / pagination.limit);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Tài xế</h1>
          <p className="text-sm text-gray-500 mt-0.5">{pagination.total} tài xế</p>
        </div>
      </div>

      {/* Search */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 mb-4">
        <form onSubmit={handleSearch} className="flex gap-2">
          <div className="relative flex-1">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              name="searchInput"
              type="text"
              placeholder="Tìm theo tên, email, biển số xe..."
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
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500 text-xs uppercase bg-gray-50">
                <th className="px-5 py-3">ID</th>
                <th className="px-5 py-3">Thông tin</th>
                <th className="px-5 py-3">Phương tiện</th>
                <th className="px-5 py-3">Đánh giá</th>
                <th className="px-5 py-3">Chuyến đi</th>
                <th className="px-5 py-3">Thu nhập</th>
                <th className="px-5 py-3">Trạng thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {drivers.map((driver) => (
                <tr key={driver.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-5 py-3 text-gray-500">#{driver.id}</td>
                  <td className="px-5 py-3">
                    <p className="font-medium text-gray-800">{driver.name || '-'}</p>
                    <p className="text-xs text-gray-500">{driver.email}</p>
                    <p className="text-xs text-gray-400">{driver.phone || ''}</p>
                  </td>
                  <td className="px-5 py-3">
                    <div className="flex items-center gap-1.5 text-gray-700">
                      <Car size={14} className="text-gray-400" />
                      <span>{driver.car_model || '-'}</span>
                    </div>
                    <p className="text-xs text-gray-400">{driver.car_color} {driver.license_plate}</p>
                  </td>
                  <td className="px-5 py-3">
                    {driver.rating ? (
                      <span className="flex items-center gap-1 text-yellow-500">
                        <Star size={14} fill="currentColor" />
                        <span className="font-medium">{Number(driver.rating).toFixed(1)}</span>
                      </span>
                    ) : (
                      <span className="text-gray-400">-</span>
                    )}
                  </td>
                  <td className="px-5 py-3 text-gray-700">{driver.total_rides || 0}</td>
                  <td className="px-5 py-3">
                    <span className="font-medium text-green-600">
                      {formatCurrency(driver.total_earnings)}
                    </span>
                  </td>
                  <td className="px-5 py-3">
                    {driver.is_available ? (
                      <span className="px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-700">
                        Online
                      </span>
                    ) : (
                      <span className="px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-500">
                        Offline
                      </span>
                    )}
                  </td>
                </tr>
              ))}
              {!drivers.length && !loading && (
                <tr>
                  <td colSpan={7} className="px-5 py-12 text-center text-gray-400">
                    Không tìm thấy tài xế nào
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
                onClick={() => fetchDrivers(pagination.page - 1)}
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
                    onClick={() => fetchDrivers(p)}
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
                onClick={() => fetchDrivers(pagination.page + 1)}
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
