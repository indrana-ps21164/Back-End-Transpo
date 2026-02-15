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
  const [busList, setBusList] = useState([]);
  const [busLoading, setBusLoading] = useState(false);
  const [busError, setBusError] = useState('');
  const [times, setTimes] = useState([]);
  const [timesLoading, setTimesLoading] = useState(false);
  const [timesError, setTimesError] = useState('');
  const role = me?.role || 'PASSENGER';
  const username = me?.username || '';
  // Load buses (drivers see only their assigned bus; others see all)
  useEffect(() => {
    (async () => {
      try {
        setBusLoading(true); setBusError('');
        const res = await fetch('/api/buses');
        const data = await res.json();
        const items = Array.isArray(data) ? data : (data?.content ?? []);
        let options = (items || [])
          .filter(b => b && (b.busNumber || b.number))
          .map(b => ({ id: b.id, busNumber: b.busNumber || b.number }));
        // If driver, reduce to assigned bus only
        if (me?.role === 'DRIVER') {
          const assignedNumber = me?.assignedBusNumber || null;
          const assignedId = me?.assignedBusId || null;
          options = options.filter(o =>
            (assignedNumber && o.busNumber === assignedNumber) || (assignedId && o.id === assignedId)
          );
        }
        setBusList(options);
      } catch (e) {
        setBusError('Failed to load buses'); setBusList([]);
      } finally { setBusLoading(false); }
    })();
  }, []);

  // Load times when bus changes
  useEffect(() => {
    (async () => {
      if (!busNumber) { setTimes([]); setScheduleId(''); return; }
      try {
        setTimesLoading(true); setTimesError('');
        const res = await fetch(`/api/schedules?busNumber=${encodeURIComponent(busNumber)}`);
        const data = await res.json();
        const list = Array.isArray(data) ? data : [];
        setTimes(list);
        setScheduleId('');
      } catch (e) {
        setTimesError('Failed to load departure times'); setTimes([]); setScheduleId('');
      } finally { setTimesLoading(false); }
    })();
  }, [busNumber]);

  return (
    <div style={{ padding: '1rem' }}>
      <h2>Seat Availability</h2>
      <p style={{ color: '#555' }}>Select Bus Number and Departure Time to view seats.</p>
      <div style={{ display: 'flex', gap: 8, marginBottom: 12, flexWrap: 'wrap' }}>
        <select value={busNumber} onChange={e => setBusNumber(e.target.value)} disabled={busLoading || busList.length === 0}>
          <option value="">{busLoading ? 'Loading buses…' : busList.length === 0 ? 'No buses available' : 'Select Bus Number'}</option>
          {busList.map(b => (
            <option key={b.id} value={b.busNumber}>{b.busNumber}</option>
          ))}
        </select>
        <select disabled={!busNumber || timesLoading || times.length === 0} value={scheduleId} onChange={e => setScheduleId(e.target.value)}>
          <option value="">{timesLoading ? 'Loading times…' : times.length === 0 ? 'No departure times' : 'Select Departure Time'}</option>
          {times.map(t => (
            <option key={t.id} value={t.id}>{String(t.departureTime).replace('T',' ')}</option>
          ))}
        </select>
      </div>
      {(busError || timesError) && <div style={{ color: 'red', marginBottom: 8 }}>{busError || timesError}</div>}
      {busNumber && scheduleId ? (
        <SeatAvailability busNumber={busNumber} scheduleId={Number(scheduleId)} role={role} username={username} />
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
  const [profileOpen, setProfileOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [editMsg, setEditMsg] = useState('');
  const [editErr, setEditErr] = useState('');
  const [profileForm, setProfileForm] = useState({ username: '', password: '', role: '', assignedBusId: '' });
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
            <div style={{ marginLeft: 'auto', position: 'relative' }}>
              <button
                aria-label="Profile"
                title="Profile"
                onClick={() => setProfileOpen((v) => !v)}
                style={{
                  border: 'none', background: 'transparent', cursor: 'pointer',
                  display: 'flex', alignItems: 'center', gap: 8, padding: '4px 8px'
                }}
              >
                <span style={{
                  width: 28, height: 28, borderRadius: '50%', background: '#0078d4', color: '#fff',
                  display: 'inline-flex', alignItems: 'center', justifyContent: 'center', fontWeight: 600
                }}>
                  {(me?.username || '?').slice(0,1).toUpperCase()}
                </span>
                <span style={{ fontSize: '.9rem', color: '#333' }}>{me?.username || 'User'}</span>
              </button>
              {profileOpen && (
                <div
                  style={{
                    position: 'absolute', right: 0, top: '110%', background: '#fff', border: '1px solid #ddd',
                    borderRadius: 8, boxShadow: '0 4px 12px rgba(0,0,0,0.08)', minWidth: 260, zIndex: 10
                  }}
                >
                  <div style={{ padding: '0.75rem', borderBottom: '1px solid #eee' }}>
                    <div style={{ fontWeight: 600 }}>{me?.username || '—'}</div>
                    <div style={{ fontSize: '.85rem', color: '#555' }}>Role: {me?.role || '—'}</div>
                    {me?.email && (<div style={{ fontSize: '.85rem', color: '#555' }}>Email: {me.email}</div>)}
                    {me?.assignedBusNumber && (
                      <div style={{ fontSize: '.85rem', color: '#555' }}>Assigned Bus: {me.assignedBusNumber}</div>
                    )}
                    {me?.assignedBusName && (
                      <div style={{ fontSize: '.85rem', color: '#555' }}>Bus Name: {me.assignedBusName}</div>
                    )}
                  </div>
                  <div style={{ padding: '0.5rem', display: 'grid', gap: 8 }}>
                    <button onClick={() => setProfileOpen(false)}>View Profile</button>
                    <button onClick={() => {
                      setProfileOpen(false);
                      setEditMsg(''); setEditErr('');
                      setProfileForm({
                        username: me?.username || '',
                        password: '',
                        role: me?.role || '',
                        assignedBusId: ''
                      });
                      setEditOpen(true);
                    }}>Edit Profile</button>
                    <button onClick={onLogout} style={{ width: '100%' }}>Logout</button>
                  </div>
                </div>
              )}
            </div>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register" style={{ marginLeft: '0.5rem' }}>Register</Link>
          </>
        )}
      </nav>
      <main>{children}</main>
      {editOpen && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.35)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 20 }}>
          <div style={{ background: '#fff', borderRadius: 10, width: 'min(560px, 92vw)', padding: '1rem', boxShadow: '0 10px 24px rgba(0,0,0,0.2)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3 style={{ margin: 0 }}>Edit Profile</h3>
              <button onClick={() => setEditOpen(false)} aria-label="Close">✕</button>
            </div>
            {editMsg && <p style={{ color: 'green' }}>{editMsg}</p>}
            {editErr && <p style={{ color: 'red' }}>{editErr}</p>}
            <div className="form" style={{ flexWrap: 'wrap' }}>
              <input placeholder="Username" value={profileForm.username} onChange={(e) => setProfileForm({ ...profileForm, username: e.target.value })} />
              <input placeholder="New Password" type="password" value={profileForm.password} onChange={(e) => setProfileForm({ ...profileForm, password: e.target.value })} />
              {/* Role editable only for Admin */}
              {role === 'ADMIN' && (
                <select value={profileForm.role} onChange={(e) => setProfileForm({ ...profileForm, role: e.target.value })}>
                  <option value="">Role (keep)</option>
                  <option value="PASSENGER">Passenger</option>
                  <option value="CONDUCTOR">Conductor</option>
                  <option value="DRIVER">Driver</option>
                  <option value="ADMIN">Admin</option>
                </select>
              )}
              {/* Assigned Bus only for Driver/Conductor; enter ID to change */}
              {(role === 'DRIVER' || role === 'CONDUCTOR') && (
                <input placeholder="Assigned Bus ID" value={profileForm.assignedBusId} onChange={(e) => setProfileForm({ ...profileForm, assignedBusId: e.target.value.replace(/[^0-9]/g, '') })} />
              )}
              <div style={{ display: 'flex', gap: 8 }}>
                <button onClick={async () => {
                  setEditMsg(''); setEditErr('');
                  // Basic password validation
                  if (profileForm.password && profileForm.password.length < 6) {
                    setEditErr('Password too short');
                    return;
                  }
                  try {
                    const payload = { username: profileForm.username };
                    if (profileForm.password) payload.password = profileForm.password;
                    if (role === 'ADMIN' && profileForm.role) payload.role = profileForm.role;
                    if ((role === 'DRIVER' || role === 'CONDUCTOR') && profileForm.assignedBusId) payload.assignedBusId = profileForm.assignedBusId;
                    const res = await updateProfile(payload);
                    setEditMsg(res?.message || 'Profile updated');
                    // Refresh me
                    try { const m = await whoami(); setMe(m); if (m?.role) { setRole(m.role); setStoredRole(m.role); } } catch {}
                  } catch (e) {
                    setEditErr(e?.response?.data?.error || 'Failed to update');
                  }
                }}>Save</button>
                <button onClick={() => setEditOpen(false)}>Cancel</button>
              </div>
            </div>
          </div>
        </div>
      )}
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

// Allow multiple roles to access a route
const RequireAnyRole = ({ roles, children }) => {
  const current = getStoredRole();
  if (!isAuthed()) return <Navigate to="/login" replace />;
  if (!current || !Array.isArray(roles) || !roles.includes(current)) return <Navigate to="/access-denied" replace />;
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
          <Route path="/seat-availability" element={<RequireAnyRole roles={["DRIVER","PASSENGER","CONDUCTOR"]}><SeatAvailabilityPageWrapper /></RequireAnyRole>} />

          <Route path="/buses" element={<RequireRole role="ADMIN"><BusesPage /></RequireRole>} />
          <Route path="/routes" element={<RequireRole role="ADMIN"><RoutesPage /></RequireRole>} />
          <Route path="/schedules" element={<RequireAnyRole roles={["DRIVER","CONDUCTOR","ADMIN"]}><SchedulesPage /></RequireAnyRole>} />
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
  getAdminBuses,
  getDriverBusReservations,
} from './api/resources';
// Driver dashboard with live map
function DriverDashboard() {
  const [me, setMe] = useState(null);
  const [bus, setBus] = useState(null);
  const [busLoading, setBusLoading] = useState(true);
  const [newBusId, setNewBusId] = useState('');
  const [lat, setLat] = useState(null);
  const [lng, setLng] = useState(null);
  const [msg, setMsg] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const m = await whoami();
        setMe(m);
        setBusLoading(true);
        const b = await fetchDriverBus();
        // If response is empty or missing key fields, attempt to resolve via admin buses
        if (!b || (!b.busNumber && !b.number && !b.assignedBusNumber && !(b.bus && (b.bus.busNumber || b.bus.number)))) {
          try {
            const adminBuses = await getAdminBuses();
            // Find bus where assigned driver username matches current user
            const found = (adminBuses || []).find((entry) => {
              const assignedDriver = entry.assignedDriver || entry.driver || entry.assignment?.driver;
              const username = assignedDriver?.username || assignedDriver?.name || assignedDriver;
              return username && m?.username && String(username) === String(m.username);
            });
            if (found) {
              setBus({
                id: found.id,
                busNumber: found.busNumber || found.number,
                busName: found.busName || found.name,
              });
            } else {
              setBus(b);
            }
          } catch {
            setBus(b);
          }
        } else {
          setBus(b);
        }
        await refreshLocation();
      } catch (e) {
        // On error, clear loading to show fallback text
      } finally {
        setBusLoading(false);
      }
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
      {(busLoading || (bus && (bus.busNumber || bus.number || bus.assignedBusNumber || bus.bus?.busNumber || bus.bus?.number))) && (
        <p>
          <strong>Bus:</strong>{' '}
          {busLoading ? (
            'Loading bus details…'
          ) : (
            `${bus.busNumber || bus.number || bus.assignedBusNumber || bus.bus?.busNumber || bus.bus?.number} - ${bus.busName || bus.name || bus.assignedBusName || bus.bus?.busName || bus.bus?.name || ''}`
          )}
        </p>
      )}
      <div style={{ display: 'flex', gap: '.5rem', marginBottom: '.5rem' }}>
        <button onClick={sendLocationFromBrowser}>Update My Live Location</button>
        <button onClick={refreshLocation}>Refresh</button>
      </div>
  {/* Removed Change Assigned Bus ID section per request */}
      {msg && <p>{msg}</p>}
      <LiveMap lat={lat} lng={lng} />
      <div style={{ marginTop: '1rem', borderTop: '1px solid #eee', paddingTop: '1rem' }}>
        <DriverPickupMap />
      </div>
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
// Driver: Pickup points map with bus + schedule selection
import { getSeatAvailability } from './api/resources';
function DriverPickupMap() {
  const [me, setMe] = useState(null);
  const [busOptions, setBusOptions] = useState([]);
  const [busNumber, setBusNumber] = useState('');
  const [times, setTimes] = useState([]);
  const [scheduleId, setScheduleId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [markers, setMarkers] = useState([]);
  const [mapEl, setMapEl] = useState(null);

  useEffect(() => { (async () => { try { const m = await whoami(); setMe(m); } catch {} })(); }, []);
  // Load assigned bus only for driver
  useEffect(() => { (async () => {
    try {
      const b = await fetch('/api/driver/my-bus');
      const data = await b.json();
      const bn = data?.busNumber || data?.number;
      const id = data?.id || data?.bus?.id;
      const opts = bn ? [{ id, busNumber: bn }] : [];
      setBusOptions(opts);
      setBusNumber(bn || '');
    } catch (e) { setError('Failed to load assigned bus'); }
  })(); }, []);

  // Load times for selected bus
  useEffect(() => { (async () => {
    setTimes([]); setScheduleId('');
    if (!busNumber) return;
    try {
      const res = await fetch(`/api/schedules?busNumber=${encodeURIComponent(busNumber)}`);
      const data = await res.json();
      setTimes(Array.isArray(data) ? data : []);
    } catch (e) { setError('Failed to load schedules'); }
  })(); }, [busNumber]);

  // Fetch pickup markers when schedule changes
  useEffect(() => { (async () => {
    if (!scheduleId || !busNumber) { setMarkers([]); return; }
    setLoading(true); setError('');
    try {
      const res = await fetch(`/api/driver/pickups?scheduleId=${scheduleId}&busNumber=${encodeURIComponent(busNumber)}`);
      const data = await res.json();
      setMarkers(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to load pickup points');
      setMarkers([]);
    } finally { setLoading(false); }
  })(); }, [scheduleId, busNumber]);

  // Render Leaflet map
  useEffect(() => { (async () => {
    if (!mapEl) return;
    const id = 'leaflet-css';
    if (!document.getElementById(id)) {
      const link = document.createElement('link');
      link.id = id; link.rel = 'stylesheet';
      link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
      document.head.appendChild(link);
    }
    const Lmod = await import('leaflet');
    const L = Lmod.default || Lmod;
    const map = L.map(mapEl, { zoomControl: true });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '&copy; OpenStreetMap contributors' }).addTo(map);
    const bounds = [];
    (markers || []).forEach(m => {
      if (m.lat != null && m.lng != null) {
        const latlng = [m.lat, m.lng];
        bounds.push(latlng);
        const mk = L.marker(latlng).addTo(map);
        const name = m.passengerName || 'Passenger';
        const seat = m.seatNumber != null ? `Seat ${m.seatNumber}` : '';
        const stop = m.pickupName || 'Pickup';
        mk.bindPopup(`<b>${name}</b><br/>${seat}<br/>${stop}`);
      }
    });
    if (bounds.length > 0) {
      map.fitBounds(L.latLngBounds(bounds), { padding: [20, 20] });
    } else {
      map.setView([6.9271, 79.8612], 11);
    }
  })(); }, [mapEl, markers]);

  if (me && me.role !== 'DRIVER') return null;

  return (
    <div>
      <h3>Pickup Points Map</h3>
      <div style={{ display: 'flex', gap: 8, marginBottom: 8, flexWrap: 'wrap' }}>
        <select value={busNumber} onChange={e => setBusNumber(e.target.value)} disabled={!busOptions.length}>
          <option value="">{!busOptions.length ? 'No assigned bus' : 'Select Bus Number'}</option>
          {busOptions.map(b => (<option key={b.id} value={b.busNumber}>{b.busNumber}</option>))}
        </select>
        <select value={scheduleId} onChange={e => setScheduleId(e.target.value)} disabled={!busNumber || !times.length}>
          <option value="">{!busNumber ? 'Select bus first' : (!times.length ? 'No departure times' : 'Select Departure Time')}</option>
          {times.map(t => (<option key={t.id} value={t.id}>{String(t.departureTime).replace('T',' ')}</option>))}
        </select>
      </div>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {!loading && markers.length === 0 && scheduleId && (
        <p style={{ color: '#666' }}>No reservations found or no pickup points available.</p>
      )}
      <div ref={setMapEl} style={{ height: 420, border: '1px solid #ddd', borderRadius: 8 }} />
    </div>
  );
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
  const [busQuery, setBusQuery] = useState('');
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

  // Client-side filtering by bus number (case-insensitive, partial match)
  const filteredItems = (items || []).filter((s) => {
    const busNumber = (s.busNumber || s.bus?.number || s.bus?.busNumber || '').toString();
    return busNumber.toLowerCase().includes(busQuery.trim().toLowerCase());
  });

  const handleScheduleChange = (id, field, value) => {
    setItems(prev => prev.map(s => (s.id === id ? { ...s, [field]: value } : s)));
  };

  if (loading) return <p>Loading...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;

  const groups = filteredItems.reduce((acc, s) => {
    const key = s.routeName || s.route?.name || s.routeId || 'Route';
    (acc[key] ||= []).push(s);
    return acc;
  }, {});

  return (
    <div>
      <h2>Schedules</h2>
      <div className="form" style={{ marginBottom: '1rem' }}>
        <label><strong>Search by Bus Number</strong></label>
        <input placeholder="Search by Bus Number" value={busQuery} onChange={(e) => setBusQuery(e.target.value)} />
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
  const [busReservations, setBusReservations] = useState([]);
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
  const [role, setRole] = useState(getStoredRole());
  const [driverBus, setDriverBus] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        setError('');
        setSuccess('');
        const me = localStorage.getItem('token');
        if (me) {
          // Auto-fill passenger name from logged-in user
          setPassengerName(prev => prev && prev.length > 0 ? prev : me);
          // Load my reservations via backend filter
          try {
            const resp = await fetch('/api/reservations/my');
            const data = await resp.json();
            setMine(Array.isArray(data) ? data : (data?.content ?? []));
          } catch {}
        }
        // Load role and driver bus for Bus Reservations section
        const currentRole = getStoredRole();
        setRole(currentRole);
        if (currentRole === 'DRIVER') {
          try {
            const bus = await fetchDriverBus();
            setDriverBus(bus);
            const driverRes = await getDriverBusReservations();
            setBusReservations(Array.isArray(driverRes) ? driverRes : (driverRes?.content ?? []));
          } catch {}
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
      try {
        const resp = await fetch('/api/reservations/my');
        const data = await resp.json();
        setMine(Array.isArray(data) ? data : (data?.content ?? []));
      } catch {}
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
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(280px,1fr))', gap: '1rem', marginTop: '0.5rem' }}>
          {mine.map((r) => (
            <div key={r.id} style={{ border: '1px solid #ddd', borderRadius: 10, padding: '0.9rem', background: '#fff' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <strong>#{r.id}</strong>
                <span style={{ fontSize: '.8rem', color: r.status === 'PAID' ? '#1e88e5' : '#e53935' }}>{r.status || 'RESERVED'}</span>
              </div>
              <div style={{ fontSize: '.9rem', color: '#333', marginTop: '.35rem', lineHeight: 1.5 }}>
                <div><strong>Passenger:</strong> {r.passengerName}</div>
                <div><strong>Bus:</strong> {r.busNumber || '—'}</div>
                <div><strong>Seat:</strong> {r.seatNumber}</div>
                <div><strong>Departure:</strong> {r.departureTime ? String(r.departureTime).replace('T',' ') : '—'}</div>
                <div><strong>Schedule:</strong> {r.scheduleId}</div>
                {r.pickup && (<div><strong>Pickup:</strong> {r.pickup}</div>)}
                {r.drop && (<div><strong>Drop:</strong> {r.drop}</div>)}
                {r.bookingTime && (
                  <div style={{ fontSize: '.8rem', color: '#777', marginTop: '.25rem' }}>Booked: {String(r.bookingTime).replace('T', ' ')}</div>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p style={{ color: '#666' }}>No Reservations</p>
      )}

      {/* Bus Reservations (DRIVER only) */}
      {role === 'DRIVER' && (
        <>
          <h3 style={{ marginTop: '1rem' }}>Bus Reservations</h3>
          {driverBus ? (
            busReservations && busReservations.length > 0 ? (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(260px,1fr))', gap: '1rem', marginTop: '0.5rem' }}>
                {busReservations.map((r) => (
                  <div
                    key={r.id}
                    style={{
                      border: '1px solid #ddd',
                      borderRadius: 8,
                      padding: '0.75rem 1rem',
                      boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
                      background: '#fff',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
                      <strong>Reservation #{r.id}</strong>
                      <span style={{ fontSize: '.8rem', color: '#666' }}>
                        Bus: {r.busNumber || r.bus?.busNumber || r.bus?.number || 'N/A'}
                      </span>
                    </div>
                    <div style={{ fontSize: '.9rem', color: '#444', lineHeight: 1.4 }}>
                      <div><strong>Schedule:</strong> {r.scheduleId}</div>
                      <div><strong>Name:</strong> {r.passengerName}</div>
                      <div><strong>Email:</strong> {r.passengerEmail}</div>
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
              <p>No Bus Reservations for assigned bus {driverBus.busNumber || driverBus.number}</p>
            )
          ) : (
            <p>Loading assigned bus...</p>
          )}
        </>
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
