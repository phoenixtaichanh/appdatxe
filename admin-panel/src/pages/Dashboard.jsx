import { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';
import {
  Users, Car, Receipt, TrendingUp, Clock, CheckCircle,
  XCircle, AlertCircle, ArrowUpRight, ArrowDownRight, RefreshCw
} from 'lucide-react';

function StatCard({ icon: Icon, label, value, sub, color, bgColor }) {
  return (
    <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100 flex items-start gap-4">
      <div className={`p-3 rounded-xl ${bgColor}`}>
        <Icon size={22} className={color} />
      </div>
      <div>
        <p className="text-sm text-gray-500">{label}</p>
        <p className="text-2xl font-bold text-gray-800 mt-0.5">{value}</p>
        {sub && <p className="text-xs text-gray-400 mt-0.5">{sub}</p>}
      </div>
    </div>
  );
}

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

const RIDE_STATUS_COLORS = {
  pending: 'bg-yellow-100 text-yellow-700',
  accepted: 'bg-blue-100 text-blue-700',
  arrived: 'bg-indigo-100 text-indigo-700',
  in_progress: 'bg-purple-100 text-purple-700',
  completed: 'bg-green-100 text-green-700',
  cancelled: 'bg-red-100 text-red-700',
};

const RIDE_STATUS_LABELS = {
  pending: 'Chờ nhận',
  accepted: 'Đã nhận',
  arrived: 'Đã đến',
  in_progress: 'Đang đi',
  completed: 'Hoàn thành',
  cancelled: 'Đã hủy',
};

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [revenue, setRevenue] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchData = async () => {
    try {
      const [dashRes, revRes] = await Promise.all([
        adminAPI.dashboard(),
        adminAPI.statsRevenue(),
      ]);
      setData(dashRes.data.data);
      setRevenue(revRes.data.data);
    } catch (err) {
      console.error('Failed to load dashboard:', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleRefresh = () => {
    setRefreshing(true);
    fetchData();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full" />
      </div>
    );
  }

  const d = data || {};
  const r = revenue || {};
  const changeClass = r.change_percent >= 0 ? 'text-green-600' : 'text-red-600';
  const ChangeIcon = r.change_percent >= 0 ? ArrowUpRight : ArrowDownRight;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Dashboard</h1>
          <p className="text-sm text-gray-500 mt-0.5">Tổng quan hệ thống</p>
        </div>
        <button
          onClick={handleRefresh}
          className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 rounded-lg text-sm text-gray-600 hover:bg-gray-50 transition-colors"
        >
          <RefreshCw size={15} className={refreshing ? 'animate-spin' : ''} />
          Làm mới
        </button>
      </div>

      {/* Stats grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard
          icon={Users}
          label="Tổng hành khách"
          value={d.users?.total_passengers || 0}
          sub={`${d.users?.active_passengers || 0} đang hoạt động`}
          color="text-blue-600"
          bgColor="bg-blue-50"
        />
        <StatCard
          icon={Car}
          label="Tổng tài xế"
          value={d.users?.total_drivers || 0}
          sub={`${d.users?.active_drivers || 0} đang online`}
          color="text-green-600"
          bgColor="bg-green-50"
        />
        <StatCard
          icon={Receipt}
          label="Tổng chuyến đi"
          value={d.rides?.total || 0}
          sub={`${d.rides?.completed || 0} hoàn thành`}
          color="text-purple-600"
          bgColor="bg-purple-50"
        />
        <StatCard
          icon={TrendingUp}
          label="Doanh thu hôm nay"
          value={formatCurrency(d.today?.revenue || 0)}
          sub={r.change_percent !== undefined ? (
            <span className={`flex items-center gap-0.5 ${changeClass}`}>
              <ChangeIcon size={12} />
              {Math.abs(r.change_percent)}% so với hôm qua
            </span>
          ) : ''}
          color="text-orange-600"
          bgColor="bg-orange-50"
        />
      </div>

      {/* Secondary stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard
          icon={Clock}
          label="Chuyến hôm nay"
          value={d.today?.rides || 0}
          color="text-cyan-600"
          bgColor="bg-cyan-50"
        />
        <StatCard
          icon={CheckCircle}
          label="Hoàn thành"
          value={d.rides?.completed || 0}
          color="text-green-600"
          bgColor="bg-green-50"
        />
        <StatCard
          icon={AlertCircle}
          label="Đang chờ"
          value={d.rides?.pending || 0}
          color="text-yellow-600"
          bgColor="bg-yellow-50"
        />
        <StatCard
          icon={XCircle}
          label="Đã hủy"
          value={d.rides?.cancelled || 0}
          color="text-red-600"
          bgColor="bg-red-50"
        />
      </div>

      {/* Revenue summary */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <p className="text-sm text-gray-500">Hôm nay</p>
          <p className="text-xl font-bold text-gray-800 mt-1">{formatCurrency(r.today)}</p>
        </div>
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <p className="text-sm text-gray-500">Tháng này</p>
          <p className="text-xl font-bold text-gray-800 mt-1">{formatCurrency(r.this_month)}</p>
        </div>
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <p className="text-sm text-gray-500">Tháng trước</p>
          <p className="text-xl font-bold text-gray-800 mt-1">{formatCurrency(r.last_month)}</p>
        </div>
      </div>

      {/* Recent rides */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100">
        <div className="px-5 py-4 border-b border-gray-100">
          <h2 className="font-semibold text-gray-800">Chuyến đi gần đây</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500 text-xs uppercase bg-gray-50">
                <th className="px-5 py-3">ID</th>
                <th className="px-5 py-3">Khách</th>
                <th className="px-5 py-3">Tài xế</th>
                <th className="px-5 py-3">Tuyến đường</th>
                <th className="px-5 py-3">Giá</th>
                <th className="px-5 py-3">Trạng thái</th>
                <th className="px-5 py-3">Thời gian</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {d.recent_rides?.map((ride) => (
                <tr key={ride.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-5 py-3 text-gray-500">#{ride.id}</td>
                  <td className="px-5 py-3 font-medium text-gray-800">{ride.passenger_name || '-'}</td>
                  <td className="px-5 py-3 text-gray-700">{ride.driver_name || '-'}</td>
                  <td className="px-5 py-3 text-gray-600 max-w-xs truncate">
                    {ride.pickup_address || '-'}
                    {ride.pickup_address && ride.dropoff_address && (
                      <span className="text-gray-400 mx-1">→</span>
                    )}
                    {ride.dropoff_address || ''}
                  </td>
                  <td className="px-5 py-3 font-medium text-gray-800">{formatCurrency(ride.price)}</td>
                  <td className="px-5 py-3">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${RIDE_STATUS_COLORS[ride.status] || 'bg-gray-100 text-gray-600'}`}>
                      {RIDE_STATUS_LABELS[ride.status] || ride.status}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-gray-500">{formatDate(ride.created_at)}</td>
                </tr>
              ))}
              {!d.recent_rides?.length && (
                <tr>
                  <td colSpan={7} className="px-5 py-8 text-center text-gray-400">Không có chuyến đi nào</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
