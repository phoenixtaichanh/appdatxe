import { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  LineChart, Line, Legend,
} from 'recharts';
import { RefreshCw, TrendingUp, TrendingDown } from 'lucide-react';

function formatCurrency(num) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(num || 0);
}

function formatShortCurrency(num) {
  if (num >= 1000000) return `${(num / 1000000).toFixed(1)}M`;
  if (num >= 1000) return `${(num / 1000).toFixed(0)}K`;
  return num.toString();
}

const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg text-sm">
        <p className="font-semibold text-gray-700 mb-1">{label}</p>
        {payload.map((entry, i) => (
          <p key={i} className="text-gray-600">
            <span className="font-medium" style={{ color: entry.color }}>{entry.name}: </span>
            {entry.name.includes('Doanh thu') || entry.name === 'Revenue'
              ? formatCurrency(entry.value)
              : entry.value}
          </p>
        ))}
      </div>
    );
  }
  return null;
};

export default function Statistics() {
  const [dailyData, setDailyData] = useState([]);
  const [revenue, setRevenue] = useState(null);
  const [days, setDays] = useState(30);
  const [loading, setLoading] = useState(true);
  const [chartType, setChartType] = useState('bar');

  const fetchData = async () => {
    setLoading(true);
    try {
      const [dailyRes, revRes] = await Promise.all([
        adminAPI.statsDaily(days),
        adminAPI.statsRevenue(),
      ]);
      setDailyData(dailyRes.data.data || []);
      setRevenue(revRes.data.data);
    } catch (err) {
      console.error('Failed to load statistics:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [days]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full" />
      </div>
    );
  }

  const r = revenue || {};
  const changeClass = r.change_percent >= 0 ? 'text-green-600' : 'text-red-600';
  const ChangeIcon = r.change_percent >= 0 ? TrendingUp : TrendingDown;

  const chartData = dailyData.map(d => ({
    date: new Date(d.date).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }),
    'Tổng chuyến': d.total_rides,
    'Hoàn thành': d.completed,
    'Doanh thu (VNĐ)': d.revenue,
  }));

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Thống kê</h1>
          <p className="text-sm text-gray-500 mt-0.5">Doanh thu và hoạt động</p>
        </div>
        <div className="flex gap-2">
          <select
            value={days}
            onChange={(e) => setDays(parseInt(e.target.value))}
            className="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none"
          >
            <option value={7}>7 ngày</option>
            <option value={14}>14 ngày</option>
            <option value={30}>30 ngày</option>
            <option value={60}>60 ngày</option>
            <option value={90}>90 ngày</option>
          </select>
          <button
            onClick={fetchData}
            className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 rounded-lg text-sm text-gray-600 hover:bg-gray-50 transition-colors"
          >
            <RefreshCw size={15} />
            Làm mới
          </button>
        </div>
      </div>

      {/* Revenue summary cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <p className="text-sm text-gray-500">Hôm nay</p>
          <p className="text-xl font-bold text-gray-800 mt-1">{formatCurrency(r.today)}</p>
          {r.change_percent !== undefined && (
            <p className={`text-xs font-medium mt-1 flex items-center gap-0.5 ${changeClass}`}>
              <ChangeIcon size={12} />
              {Math.abs(r.change_percent)}% so với hôm qua
            </p>
          )}
        </div>
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <p className="text-sm text-gray-500">Hôm qua</p>
          <p className="text-xl font-bold text-gray-800 mt-1">{formatCurrency(r.yesterday)}</p>
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

      {/* Chart type toggle */}
      <div className="flex gap-2 mb-4">
        <button
          onClick={() => setChartType('bar')}
          className={`px-3 py-1.5 text-sm rounded-lg transition-colors ${
            chartType === 'bar' ? 'bg-blue-600 text-white' : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
          }`}
        >
          Biểu đồ cột
        </button>
        <button
          onClick={() => setChartType('line')}
          className={`px-3 py-1.5 text-sm rounded-lg transition-colors ${
            chartType === 'line' ? 'bg-blue-600 text-white' : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
          }`}
        >
          Biểu đồ đường
        </button>
      </div>

      {/* Main chart - Revenue */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5 mb-4">
        <h2 className="font-semibold text-gray-800 mb-4">Doanh thu theo ngày</h2>
        {chartData.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            {chartType === 'bar' ? (
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                <YAxis tickFormatter={formatShortCurrency} tick={{ fontSize: 11 }} />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey="Doanh thu (VNĐ)" fill="#3b82f6" radius={[4, 4, 0, 0]} />
              </BarChart>
            ) : (
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                <YAxis tickFormatter={formatShortCurrency} tick={{ fontSize: 11 }} />
                <Tooltip content={<CustomTooltip />} />
                <Line type="monotone" dataKey="Doanh thu (VNĐ)" stroke="#3b82f6" strokeWidth={2} dot={{ r: 3 }} />
              </LineChart>
            )}
          </ResponsiveContainer>
        ) : (
          <div className="flex items-center justify-center h-64 text-gray-400">
            Chưa có dữ liệu thống kê
          </div>
        )}
      </div>

      {/* Secondary chart - Rides */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
        <h2 className="font-semibold text-gray-800 mb-4">Số chuyến đi theo ngày</h2>
        {chartData.length > 0 ? (
          <ResponsiveContainer width="100%" height={280}>
            {chartType === 'bar' ? (
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip content={<CustomTooltip />} />
                <Legend />
                <Bar dataKey="Tổng chuyến" fill="#6366f1" radius={[4, 4, 0, 0]} />
                <Bar dataKey="Hoàn thành" fill="#22c55e" radius={[4, 4, 0, 0]} />
              </BarChart>
            ) : (
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip content={<CustomTooltip />} />
                <Legend />
                <Line type="monotone" dataKey="Tổng chuyến" stroke="#6366f1" strokeWidth={2} dot={{ r: 3 }} />
                <Line type="monotone" dataKey="Hoàn thành" stroke="#22c55e" strokeWidth={2} dot={{ r: 3 }} />
              </LineChart>
            )}
          </ResponsiveContainer>
        ) : (
          <div className="flex items-center justify-center h-64 text-gray-400">
            Chưa có dữ liệu thống kê
          </div>
        )}
      </div>
    </div>
  );
}
