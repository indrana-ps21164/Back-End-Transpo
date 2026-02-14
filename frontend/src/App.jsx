import { BrowserRouter, Routes, Route, Link, Navigate, useNavigate } from 'react-router-dom';
import AdminDashboard from './components/AdminDashboard';
import { whoami } from './api/auth';
import SeatAvailability from './components/SeatAvailability';
import './App.css';
// Role-based menu config and helpers
const MENU_CONFIG = {
  DRIVER: [
    { path: '/', label: 'Dashboard' },
    { path: '/schedules', label: 'Schedules' },
    { path: '/reservations', label: 'Reservations' },
    { path: '/seat-availability', label: 'Seat Availability' },
    { path: '/driver', label: 'Driver' },
  ],
  PASSENGER: [
    { path: '/', label: 'Dashboard' },
    { path: '/reservations', label: 'Reservations' },
    { path: '/seat-availability', label: 'Seat Availability' },
  ],
  CONDUCTOR: [
    { path: '/', label: 'Dashboard' },
    { path: '/schedules', label: 'Schedules' },
    { path: '/reservations', label: 'Reservations' },
    { path: '/seat-availability', label: 'Seat Availability' },
  ],
  ADMIN: [
    { path: '/', label: 'Dashboard' },
    { path: '/admin', label: 'Admin' },
    { path: '/buses', label: 'Buses' },
    { path: '/routes', label: 'Routes' },
    { path: '/schedules', label: 'Schedules' },
    { path: '/reservations', label: 'Reservations' },
  ],
};
const getStoredRole = () => sessionStorage.getItem('role') || localStorage.getItem('role') || null;
const setStoredRole = (role) => { sessionStorage.setItem('role', role); localStorage.setItem('role', role); };
const clearStoredRole = () => { sessionStorage.removeItem('role'); localStorage.removeItem('role'); };

function AccessDenied() {
  return (
    <div style={{ padding: '1rem' }}>
      <h2>Access Denied</h2>
      <p>You do not have permission to view this page.</p>
      <Link to="/">Go to Dashboard</Link>
    </div>
  );
}

// Simple auth util
const isAuthed = () => !!localStorage.getItem('token');
function SeatAvailabilityPageWrapper() {
  const [me, setMe] = useState(null);
  useEffect(() => { (async () => { try { const m = await whoami(); setMe(m); } catch {} })(); }, []);
  const [busNumber, setBusNumber] = useState('');
  const [scheduleId, setScheduleId] = useState('');
  const role = me?.role || 'PASSENGER';
  const username = me?.username || '';
  return (
    <div style={{ padding: '1rem' }}>
      <h2>Seat Availability</h2>
  <p style={{ color: '#555' }}>View seat grid. Enter Bus Number and optional Schedule ID.</p>
      <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
        <input placeholder="Bus Number" value={busNumber} onChange={e => setBusNumber(e.target.value)} />
        <input placeholder="Schedule ID (optional)" value={scheduleId} onChange={e => setScheduleId(e.target.value)} />
      </div>
      {busNumber ? (
        <SeatAvailability busNumber={busNumber} scheduleId={scheduleId ? Number(scheduleId) : undefined} role={role} username={username} />
      ) : (
        <div style={{ border: '1px dashed #ccc', padding: '1rem', borderRadius: 8 }}>
          <strong>Placeholder:</strong> Seat grid will appear here.
        </div>
      )}
    </div>
  );
}

const Layout = ({ children }) => {
  const [me, setMe] = useState(null);
  const [role, setRole] = useState(getStoredRole());
  useEffect(() => {
    (async () => {
      try {
        const m = await whoami();
        setMe(m);
        if (m?.role) { setRole(m.role); setStoredRole(m.role); }
      } catch {}
    })();
  }, []);
  const authed = isAuthed();
  const menus = role && MENU_CONFIG[role] ? MENU_CONFIG[role] : [];
  const onLogout = () => { localStorage.removeItem('token'); clearStoredRole(); window.location.href = '/login'; };
  return (
    <div className="container">
      <nav className="nav">
        {authed ? (
          <>
            {menus.map((m) => (<Link key={m.path} to={m.path}>{m.label}</Link>))}
            <button onClick={onLogout} style={{ marginLeft: 'auto' }}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register" style={{ marginLeft: '0.5rem' }}>Register</Link>
          </>
        )}
      </nav>
      <main>{children}</main>
    </div>
  );
};

const RequireAuth = ({ children }) => {
  if (!isAuthed()) return <Navigate to="/login" replace />;
  return children;
};

const RequireRole = ({ role, children }) => {
  const current = getStoredRole();
  if (!isAuthed()) return <Navigate to="/login" replace />;
  if (!current || current !== role) return <Navigate to="/access-denied" replace />;
  return children;
};

import PassengerDashboard from './components/PassengerDashboard';
import ConductorDashboard from './components/ConductorDashboard';
const Dashboard = () => (
  <PassengerDashboard />
);

export default function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<RequireAuth><Dashboard /></RequireAuth>} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/seat-availability" element={<RequireAuth><SeatAvailabilityPageWrapper /></RequireAuth>} />

          <Route path="/buses" element={<RequireRole role="ADMIN"><BusesPage /></RequireRole>} />
          <Route path="/routes" element={<RequireRole role="ADMIN"><RoutesPage /></RequireRole>} />
          <Route path="/schedules" element={<RequireAuth><SchedulesPage /></RequireAuth>} />
          <Route path="/reservations" element={<RequireAuth><ReservationsPage /></RequireAuth>} />
          <Route path="/map" element={<RequireAuth><MapPage /></RequireAuth>} />
          <Route path="/driver" element={<RequireRole role="DRIVER"><DriverDashboard /></RequireRole>} />
          <Route path="/admin" element={<RequireRole role="ADMIN"><AdminDashboard /></RequireRole>} />
          <Route path="/conductor" element={<RequireRole role="CONDUCTOR"><ConductorDashboard /></RequireRole>} />
          <Route path="/access-denied" element={<AccessDenied />} />
          <Route path="*" element={<Navigate to={isAuthed() ? '/' : '/login'} replace />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

// Lazy simple pages
import { useState, useEffect } from 'react';
import { login, register } from './api/auth';
import {
  getBuses,
  createBus,
  updateBus,
  deleteBus,
  getRoutes,
  createRoute,
  updateRoute,
  deleteRoute,
  getSchedules,
  createSchedule,
  updateSchedule,
  deleteSchedule,
  getReservations,
  createReservation,
  deleteReservation,
  getMapData,
  getReservationsByUser,
} from './api/resources';
// Driver dashboard with live map
function DriverDashboard() {
  const [me, setMe] = useState(null);
  const [bus, setBus] = useState(null);
  const [newBusId, setNewBusId] = useState('');
  const [lat, setLat] = useState(null);
  const [lng, setLng] = useState(null);
  const [msg, setMsg] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const m = await whoami();
        setMe(m);
        const b = await fetchDriverBus();
        setBus(b);
        await refreshLocation();
      } catch {}
    })();
    const id = setInterval(refreshLocation, 5000);
    return () => clearInterval(id);
  }, []);

  async function refreshLocation() {
    try {
      const info = await getMyLocation();
      if (info?.lat && info?.lng) {
        setLat(info.lat); setLng(info.lng);
      }
    } catch {}
  }

  async function sendLocationFromBrowser() {
    if (!navigator.geolocation) {
      setMsg('Geolocation not supported');
      return;
    }
    navigator.geolocation.getCurrentPosition(async (pos) => {
      const { latitude, longitude } = pos.coords;
      try {
        await updateMyLocation(latitude, longitude);
        setMsg('Location updated');
        await refreshLocation();
      } catch (e) {
        setMsg(e?.response?.data?.message || 'Failed to update');
      }
    }, () => setMsg('Permission denied'));
  }

  return (
    <div>
      <h2>Driver Dashboard</h2>
      <p><strong>User:</strong> {me?.username} ({me?.role})</p>
      <p><strong>Bus:</strong> {bus ? `${bus.busNumber} - ${bus.busName}` : 'No bus assigned'}</p>
      <div style={{ display: 'flex', gap: '.5rem', marginBottom: '.5rem' }}>
        <button onClick={sendLocationFromBrowser}>Update My Live Location</button>
        <button onClick={refreshLocation}>Refresh</button>
      </div>
      <div className="form" style={{ gap: '.5rem', marginBottom: '.5rem' }}>
        <label><strong>Change Assigned Bus ID</strong></label>
        <input type="number" value={newBusId} onChange={(e) => setNewBusId(e.target.value)} placeholder="Bus ID" />
        <button onClick={async () => {
          if (!newBusId) return;
          try {
            const updated = await changeDriverBus(Number(newBusId));
            setBus(updated);
            setMsg('Assigned bus updated');
          } catch (e) {
            setMsg(e?.response?.data?.message || 'Failed to change bus');
          }
        }}>Change Bus</button>
      </div>
      {msg && <p>{msg}</p>}
      <LiveMap lat={lat} lng={lng} />
    </div>
  );
}

function LiveMap({ lat, lng }) {
  const [mapEl, setMapEl] = useState(null);
  useEffect(() => {
    if (!mapEl) return;
    // Dynamically import Leaflet to avoid SSR issues
    (async () => {
  // Import Leaflet from package
  const L = await import('leaflet');
      // Ensure Leaflet CSS is loaded
      const cssId = 'leaflet-css';
      if (!document.getElementById(cssId)) {
        const link = document.createElement('link');
        link.id = cssId;
        link.rel = 'stylesheet';
        link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
        document.head.appendChild(link);
      }
      const map = L.map(mapEl).setView([lat ?? 6.9271, lng ?? 79.8612], lat && lng ? 14 : 12);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
      }).addTo(map);
      if (lat && lng) {
        L.marker([lat, lng]).addTo(map).bindPopup('My Location');
      }
    })();
  }, [mapEl, lat, lng]);
  return <div ref={setMapEl} style={{ height: 400, border: '1px solid #ddd' }} />;
}
// Passenger dashboard components moved to components/PassengerDashboard.jsx to avoid scope collisions
// (Old DashboardMap removed to avoid duplicate identifiers)

// Generic table to render arrays of objects
function DataTable({ items }) {
  if (!items || items.length === 0) return <p>No data</p>;
  const headers = Array.from(items.reduce((set, item) => {
    Object.keys(item || {}).forEach(k => set.add(k));
    return set;
  }, new Set()));
  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            {headers.map(h => (
              <th key={h} style={{ textAlign: 'left', borderBottom: '1px solid #ccc', padding: '.5rem' }}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {items.map((row, idx) => (
            <tr key={row.id ?? idx}>
              {headers.map(h => (
                <td key={h} style={{ borderBottom: '1px solid #eee', padding: '.5rem' }}>
                  {typeof row[h] === 'object' ? JSON.stringify(row[h]) : String(row[h])}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const data = await login(username, password);
      // Backend returns message/username/roles; no token. Store username as session marker.
  localStorage.setItem('token', data.username || '');
  const role = (data?.roles?.[0] || data?.role || '').toUpperCase();
  if (role) setStoredRole(role);
      window.location.href = '/';
    } catch (err) {
  const resp = err?.response?.data;
  setError(resp?.error || resp?.message || 'Login failed');
    }
  };
  return (
    <div>
      <h2>Login</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <form onSubmit={onSubmit} className="form">
        <input placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} />
        <input placeholder="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <button type="submit">Login</button>
      </form>
      <p>
        No account? <Link to="/register">Register</Link>
      </p>
    </div>
  );
}

function RegisterPage() {
  const [payload, setPayload] = useState({ username: '', password: '', role: '' });
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const onSubmit = async (e) => {
    e.preventDefault();
    setMsg('');
    setError('');
    if (!payload.username || !payload.password) {
      setError('Username and Password are required');
      return;
    }
    try {
      setLoading(true);
      const data = await register(payload);
      setMsg(data.message || 'Registered');
  const role = (payload.role || '').toUpperCase();
  if (role) setStoredRole(role);
    } catch (err) {
      const resp = err?.response?.data;
      setError(resp?.error || resp?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };
  return (
    <div>
      <h2>Register</h2>
      {msg && <p>{msg}</p>}
      {error && <p style={{ color: 'red', marginTop: '.5rem' }}>{error}</p>}
      <form onSubmit={onSubmit} className="form">
        <input placeholder="Username" value={payload.username} onChange={(e) => setPayload({ ...payload, username: e.target.value })} />
        <input placeholder="Password" type="password" value={payload.password} onChange={(e) => setPayload({ ...payload, password: e.target.value })} />
        <select value={payload.role} onChange={(e) => setPayload({ ...payload, role: e.target.value })}>
          <option value="">Role (optional)</option>
          <option value="PASSENGER">Passenger</option>
          <option value="CONDUCTOR">Conductor</option>
          <option value="DRIVER">Driver</option>
          <option value="ADMIN">Admin</option>
        </select>
        <button type="submit" disabled={loading}>{loading ? 'Registering...' : 'Register'}</button>
      </form>
    </div>
  );
}

function BusesPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isAdmin, setIsAdmin] = useState(false);
  const [newBus, setNewBus] = useState({ busNumber: '', busName: '', totalSeats: 40 });

  useState(() => {
    (async () => {
      try {
        const data = await getBuses();
        setItems(Array.isArray(data) ? data : (data?.content ?? []));
        try { const me = await whoami(); setIsAdmin(me?.role === 'ADMIN'); } catch {}
      } catch (err) {
        setError(err?.response?.data?.message || 'Failed to load buses');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const refreshBuses = async () => {
    const data = await getBuses();
    setItems(Array.isArray(data) ? data : (data?.content ?? []));
  };

  const handleBusChange = (id, field, value) => {
    setItems(prev => prev.map(b => (b.id === id ? { ...b, [field]: value } : b)));
  };

  if (loading) return <p>Loading...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;

  return (
    <div>
      <h2>Buses</h2>
      <DataTable items={items} />

      {isAdmin && (
        <>
          <div style={{ marginTop: '1rem' }}>
            <h3>Add Bus</h3>
            <div className="form" style={{ flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                <label><strong>Bus Number</strong></label>
                <input
                  value={newBus.busNumber}
                  onChange={(e) => setNewBus({ ...newBus, busNumber: e.target.value })}
                />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                <label><strong>Bus Name</strong></label>
                <input
                  value={newBus.busName}
                  onChange={(e) => setNewBus({ ...newBus, busName: e.target.value })}
                />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                <label><strong>Total Seats</strong></label>
                <input
                  type="number"
                  value={newBus.totalSeats}
                  onChange={(e) => setNewBus({ ...newBus, totalSeats: Number(e.target.value) })}
                />
              </div>
              <button
                onClick={async () => {
                  await createBus(newBus);
                  setNewBus({ busNumber: '', busName: '', totalSeats: 40 });
                  await refreshBuses();
                }}
              >
                Add
              </button>
            </div>
          </div>

          <div style={{ marginTop: '1rem' }}>
            <h3>Admin: Edit/Delete</h3>
            {items.map((b) => (
              <div key={b.id} className="form" style={{ flexWrap: 'wrap' }}>
                <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                  <label><strong>Bus Number</strong></label>
                  <input
                    value={b.busNumber || ''}
                    onChange={(e) => handleBusChange(b.id, 'busNumber', e.target.value)}
                  />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                  <label><strong>Bus Name</strong></label>
                  <input
                    value={b.busName || ''}
                    onChange={(e) => handleBusChange(b.id, 'busName', e.target.value)}
                  />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                  <label><strong>Total Seats</strong></label>
                  <input
                    type="number"
                    value={b.totalSeats ?? 0}
                    onChange={(e) => handleBusChange(b.id, 'totalSeats', Number(e.target.value))}
                  />
                </div>
                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end' }}>
                  <button
                    onClick={async () => {
                      await updateBus(b.id, b);
                      await refreshBuses();
                    }}
                  >
                    Save
                  </button>
                  <button
                    onClick={async () => {
                      await deleteBus(b.id);
                      await refreshBuses();
                    }}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

function RoutesPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isAdmin, setIsAdmin] = useState(false);
  const [newRoute, setNewRoute] = useState({
    origin: '',
    destination: '',
    stop01: '',
    stop02: '',
    stop03: '',
    stop04: '',
    stop05: '',
    stop06: '',
    stop07: '',
    stop08: '',
    stop09: '',
    stop10: '',
  });

  useState(() => {
    (async () => {
      try {
        const data = await getRoutes();
        setItems(Array.isArray(data) ? data : (data?.content ?? []));
        try { const me = await whoami(); setIsAdmin(me?.role === 'ADMIN'); } catch {}
      } catch (err) {
        setError(err?.response?.data?.message || 'Failed to load routes');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const refreshRoutes = async () => {
    const data = await getRoutes();
    setItems(Array.isArray(data) ? data : (data?.content ?? []));
  };

  const handleRouteChange = (id, field, value) => {
    setItems(prev => prev.map(r => (r.id === id ? { ...r, [field]: value } : r)));
  };

  if (loading) return <p>Loading...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;

  return (
    <div>
      <h2>Routes</h2>
      <DataTable items={items} />

      {isAdmin && (
        <>
          <div style={{ marginTop: '1rem' }}>
            <h3>Add Route</h3>
            <div className="form" style={{ flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                <label><strong>Origin</strong></label>
                <input
                  value={newRoute.origin}
                  onChange={(e) => setNewRoute({ ...newRoute, origin: e.target.value })}
                />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                <label><strong>Destination</strong></label>
                <input
                  value={newRoute.destination}
                  onChange={(e) => setNewRoute({ ...newRoute, destination: e.target.value })}
                />
              </div>
              {[1,2,3,4,5,6,7,8,9,10].map((n) => (
                <div key={n} style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                  <label><strong>{`Stop${n.toString().padStart(2,'0')}`}</strong></label>
                  <input
                    value={newRoute[`stop${n.toString().padStart(2,'0')}`] || ''}
                    onChange={(e) =>
                      setNewRoute({
                        ...newRoute,
                        [`stop${n.toString().padStart(2,'0')}`]: e.target.value,
                      })
                    }
                  />
                </div>
              ))}
              <button
                onClick={async () => {
                  await createRoute(newRoute);
                  setNewRoute({
                    origin: '',
                    destination: '',
                    stop01: '',
                    stop02: '',
                    stop03: '',
                    stop04: '',
                    stop05: '',
                    stop06: '',
                    stop07: '',
                    stop08: '',
                    stop09: '',
                    stop10: '',
                  });
                  await refreshRoutes();
                }}
              >
                Add
              </button>
            </div>
          </div>

          <div style={{ marginTop: '1rem' }}>
            <h3>Admin: Edit/Delete</h3>
            {items.map((r) => (
              <div key={r.id} className="form" style={{ flexWrap: 'wrap' }}>
                <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                  <label><strong>Origin</strong></label>
                  <input
                    value={r.origin || ''}
                    onChange={(e) => handleRouteChange(r.id, 'origin', e.target.value)}
                  />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                  <label><strong>Destination</strong></label>
                  <input
                    value={r.destination || ''}
                    onChange={(e) => handleRouteChange(r.id, 'destination', e.target.value)}
                  />
                </div>
                {[1,2,3,4,5,6,7,8,9,10].map((n) => (
                  <div key={n} style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                    <label><strong>{`Stop${n.toString().padStart(2,'0')}`}</strong></label>
                    <input
                      value={r[`stop${n.toString().padStart(2,'0')}`] || ''}
                      onChange={(e) =>
                        handleRouteChange(
                          r.id,
                          `stop${n.toString().padStart(2,'0')}`,
                          e.target.value,
                        )
                      }
                    />
                  </div>
                ))}
                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end' }}>
                  <button
                    onClick={async () => {
                      await updateRoute(r.id, r);
                      await refreshRoutes();
                    }}
                  >
                    Save
                  </button>
                  <button
                    onClick={async () => {
                      await deleteRoute(r.id);
                      await refreshRoutes();
                    }}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

function SchedulesPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isAdmin, setIsAdmin] = useState(false);
  const [pickup, setPickup] = useState('');
  const [drop, setDrop] = useState('');
  const [newSchedule, setNewSchedule] = useState({ departureTime: '', busId: '', routeId: '' });

  useState(() => { (async () => {
    try {
      const data = await getSchedules();
      setItems(Array.isArray(data) ? data : (data?.content ?? []));
      try { const me = await whoami(); setIsAdmin(me?.role === 'ADMIN'); } catch {}
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load schedules');
    } finally {
      setLoading(false);
    }
  })(); }, []);

  const refreshSchedules = async () => {
    const data = await getSchedules();
    setItems(Array.isArray(data) ? data : (data?.content ?? []));
  };

  const onSearch = async () => {
    setError('');
    try {
      const data = await searchSchedules(pickup, drop);
      setItems(data || []);
      setLoading(false);
    } catch (e) {
      setError(e?.response?.data?.message || 'Search failed');
    }
  };

  const handleScheduleChange = (id, field, value) => {
    setItems(prev => prev.map(s => (s.id === id ? { ...s, [field]: value } : s)));
  };

  if (loading) return <p>Loading...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;

  const groups = items.reduce((acc, s) => {
    const key = s.routeName || s.route?.name || s.routeId || 'Route';
    (acc[key] ||= []).push(s);
    return acc;
  }, {});

  return (
    <div>
      <h2>Schedules</h2>
      <div className="form" style={{ marginBottom: '1rem' }}>
        <input placeholder="Pickup point" value={pickup} onChange={(e) => setPickup(e.target.value)} />
        <input placeholder="Drop point" value={drop} onChange={(e) => setDrop(e.target.value)} />
        <button onClick={onSearch}>Search</button>
      </div>

      {isAdmin && (
        <div style={{ marginBottom: '1rem' }}>
          <h3>Add Schedule</h3>
          <div className="form" style={{ flexWrap: 'wrap' }}>
            <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
              <label><strong>Departure Time</strong></label>
              <input
                value={newSchedule.departureTime}
                onChange={(e) => setNewSchedule({ ...newSchedule, departureTime: e.target.value })}
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
              <label><strong>Bus ID</strong></label>
              <input
                value={newSchedule.busId}
                onChange={(e) => setNewSchedule({ ...newSchedule, busId: e.target.value })}
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
              <label><strong>Route ID</strong></label>
              <input
                value={newSchedule.routeId}
                onChange={(e) => setNewSchedule({ ...newSchedule, routeId: e.target.value })}
              />
            </div>
            <button
              onClick={async () => {
                await createSchedule(newSchedule);
                setNewSchedule({ departureTime: '', busId: '', routeId: '' });
                await refreshSchedules();
              }}
            >
              Add
            </button>
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(280px,1fr))', gap: '1rem' }}>
        {Object.entries(groups).map(([route, list]) => (
          <div key={route} style={{ border: '1px solid #ddd', borderRadius: 8, padding: '0.75rem' }}>
            <h3 style={{ margin: '0 0 .5rem' }}>{route}</h3>
            {list.map((s) => (
              <div key={s.id} style={{ borderTop: '1px dashed #eee', padding: '.5rem 0' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <strong>{s.departureTime || s.time || '—'}</strong>
                  <span>ID: {s.id}</span>
                </div>
                <div style={{ fontSize: '.9rem', color: '#555' }}>
                  Bus: {s.busNumber || s.bus?.number || 'N/A'} · Route ID: {s.routeId || 'N/A'}
                </div>
                {/* Route details and stops */}
                <div style={{ marginTop: '.5rem', fontSize: '.9rem', color: '#333' }}>
                  <div><strong>Start:</strong> {s.origin || s.route?.origin || '—'}</div>
                  <div><strong>End:</strong> {s.destination || s.route?.destination || '—'}</div>
                  <details style={{ marginTop: '.25rem' }}>
                    <summary style={{ cursor: 'pointer' }}>Stops</summary>
                    <ol style={{ margin: '.5rem 0 0 .75rem' }}>
                      {[s.stop01, s.stop02, s.stop03, s.stop04, s.stop05, s.stop06, s.stop07, s.stop08, s.stop09, s.stop10]
                        .filter(Boolean)
                        .map((st, idx) => (
                          <li key={idx} style={{ lineHeight: 1.6 }}>{st}</li>
                        ))}
                    </ol>
                  </details>
                </div>
                {/* Passenger-only view: no inline edit/delete controls */}
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

function ReservationsPage() {
  const navigate = useNavigate();
  const [mine, setMine] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [scheduleId, setScheduleId] = useState('');
  const [passengerName, setPassengerName] = useState('');
  const [passengerEmail, setPassengerEmail] = useState('');
  const [seatNumber, setSeatNumber] = useState(1);
  const [pickupStop, setPickupStop] = useState('');
  const [dropStop, setDropStop] = useState('');
  const [paymentNumber, setPaymentNumber] = useState('');
  const [securityKey, setSecurityKey] = useState('');

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        setError('');
        setSuccess('');
        const me = localStorage.getItem('token');
        if (me) {
          try { setMine(await getReservationsByUser(me)); } catch {}
        }
        // Prefill from query params
        const params = new URLSearchParams(window.location.search);
        const sched = params.get('scheduleId');
        const p = params.get('pickup');
        const d = params.get('drop');
        if (sched) setScheduleId(sched);
        if (p) setPickupStop(p);
        if (d) setDropStop(d);
      } catch (e) {
        setError(e?.response?.data?.message || e?.message || 'Failed to load reservations');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleReserveCreate = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    // Validate fake payment inputs
    if (!/^\d{10}$/.test(paymentNumber)) {
      setError('Payment number must be 10 digits.');
      return;
    }
    if (!/^\d{3}$/.test(securityKey)) {
      setError('Security key must be 3 digits.');
      return;
    }
    try {
      await createReservation({
        scheduleId: Number(scheduleId),
        passengerName,
        passengerEmail,
        seatNumber: Number(seatNumber),
        // Pass pickup/drop names; backend may map to stop IDs
        pickup: pickupStop,
        drop: dropStop,
        status: 'BOOKED',
      });
      const me = localStorage.getItem('token');
      if (me) { try { setMine(await getReservationsByUser(me)); } catch {} }
      setSuccess('Reservation successful. Seat booked.');
      // Clear only payment inputs; keep schedule/pickup/drop visible
      setPaymentNumber(''); setSecurityKey('');
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Failed to create reservation');
    }
  };

  const handleGoBack = () => {
    // Prefer router navigate to preserve SPA history
    try {
      navigate(-1);
    } catch {
      // Fallback to browser history
      if (window.history && typeof window.history.back === 'function') {
        window.history.back();
      }
    }
  };

  if (loading) return <p>Loading...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;

  return (
    <div>
      <h2>Reservations</h2>
      <div style={{ marginBottom: '.5rem' }}>
        <button onClick={handleGoBack}>Go Back</button>
      </div>
      {success && <p style={{ color: 'green' }}>{success}</p>}
      {error && <p style={{ color: 'red' }}>{error} <button style={{ marginLeft: '.5rem' }} onClick={handleGoBack}>Go Back</button></p>}
      <form onSubmit={handleReserveCreate} className="form" style={{ flexWrap: 'wrap' }}>
        <input placeholder="Schedule ID" value={scheduleId} readOnly />
        <input placeholder="Passenger Name" value={passengerName} onChange={(e) => setPassengerName(e.target.value)} />
        <input placeholder="Passenger Email" value={passengerEmail} onChange={(e) => setPassengerEmail(e.target.value)} />
        <input placeholder="Seat Number" type="number" value={seatNumber} onChange={(e) => setSeatNumber(e.target.value)} />
        <input placeholder="Pickup Stop" value={pickupStop} onChange={(e) => setPickupStop(e.target.value)} />
        <input placeholder="Drop Stop" value={dropStop} onChange={(e) => setDropStop(e.target.value)} />
        {/* Fake payment section */}
        <input placeholder="Payment Number (10 digits)" value={paymentNumber} onChange={(e) => setPaymentNumber(e.target.value.replace(/[^0-9]/g, ''))} />
        <input placeholder="Security Key (3 digits)" value={securityKey} onChange={(e) => setSecurityKey(e.target.value.replace(/[^0-9]/g, ''))} />
        <button type="submit">Create</button>
      </form>

      <h3>My Reservations</h3>
      {mine && mine.length > 0 ? (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(260px,1fr))', gap: '1rem', marginTop: '0.5rem' }}>
          {mine.map((r) => (
            <div
              key={r.id}
              style={{
                border: '1px solid #ddd',
                borderRadius: 8,
                padding: '0.75rem 1rem',
                boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
                background: '#fafafa',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
                <strong>Reservation #{r.id}</strong>
                <span style={{ fontSize: '.8rem', color: '#666' }}>
                  Seat {r.seatNumber}
                </span>
              </div>
              <div style={{ fontSize: '.9rem', color: '#444', lineHeight: 1.4 }}>
                <div><strong>Schedule:</strong> {r.scheduleId}</div>
                <div><strong>Name:</strong> {r.passengerName}</div>
                <div><strong>Email:</strong> {r.passengerEmail}</div>
                {r.pickup && (
                  <div><strong>Pickup Stop:</strong> {r.pickup}</div>
                )}
                {r.drop && (
                  <div><strong>Drop Stop:</strong> {r.drop}</div>
                )}
                {r.bookingTime && (
                  <div style={{ fontSize: '.8rem', color: '#777', marginTop: '0.25rem' }}>
                    Booked at: {String(r.bookingTime).replace('T', ' ')}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p>No Reservations</p>
      )}
    </div>
  );
}

function MapPage() {
  const [data, setData] = useState(null);
  useState(() => { (async () => setData(await getMapData()))(); }, []);
  return (
    <div>
      <h2>Map</h2>
      <pre>{data ? JSON.stringify(data, null, 2) : 'Loading...'}</pre>
    </div>
  );
}
