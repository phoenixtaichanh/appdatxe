import { useState } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard, Users, Car, MapPin, BarChart3,
  LogOut, ChevronRight, Menu, X, UserCircle, MessageCircle
} from 'lucide-react';

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/users', label: 'Người dùng', icon: Users },
  { to: '/drivers', label: 'Tài xế', icon: Car },
  { to: '/rides', label: 'Chuyến đi', icon: MapPin },
  { to: '/statistics', label: 'Thống kê', icon: BarChart3 },
  { to: '/chat', label: 'Hỗ trợ', icon: MessageCircle },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Mobile overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-30 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`
          fixed lg:static inset-y-0 left-0 z-40 w-64 bg-slate-800 text-white flex flex-col
          transform transition-transform duration-300 ease-in-out
          ${sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
        `}
      >
        <div className="p-6 border-b border-slate-700">
          <h1 className="text-xl font-bold flex items-center gap-2">
            <span className="text-2xl">🚕</span> DoAn3 Admin
          </h1>
          <p className="text-xs text-slate-400 mt-1">Hệ thống quản lý</p>
        </div>

        <nav className="flex-1 py-4">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-6 py-3 text-sm transition-colors ${
                  isActive
                    ? 'bg-slate-700 text-white border-r-4 border-blue-500'
                    : 'text-slate-300 hover:bg-slate-700/50 hover:text-white'
                }`
              }
              onClick={() => setSidebarOpen(false)}
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="p-4 border-t border-slate-700">
          <div className="flex items-center gap-3 mb-3 px-2">
            <UserCircle size={32} className="text-slate-400" />
            <div>
              <p className="text-sm font-medium">{user?.name || 'Admin'}</p>
              <p className="text-xs text-slate-400">{user?.email}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 w-full px-3 py-2 text-sm text-slate-300 hover:text-white hover:bg-slate-700 rounded-lg transition-colors"
          >
            <LogOut size={16} />
            Đăng xuất
          </button>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Mobile header */}
        <header className="lg:hidden flex items-center justify-between px-4 py-3 bg-white shadow-sm">
          <button
            onClick={() => setSidebarOpen(true)}
            className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg"
          >
            <Menu size={20} />
          </button>
          <span className="font-semibold text-gray-800">DoAn3 Admin</span>
          <button
            onClick={handleLogout}
            className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg"
            title="Đăng xuất"
          >
            <LogOut size={20} />
          </button>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-4 lg:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
