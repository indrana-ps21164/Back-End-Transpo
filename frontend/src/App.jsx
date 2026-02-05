import { BrowserRouter, Routes, Route, Link, Navigate } from 'react-router-dom';
import './App.css';

// Simple auth util
const isAuthed = () => !!localStorage.getItem('token');

const Layout = ({ children }) => (
  <div className="container">
    <nav className="nav">
      <Link to="/">Dashboard</Link>
      <Link to="/buses">Buses</Link>
      <Link to="/routes">Routes</Link>
      <Link to="/schedules">Schedules</Link>
      <Link to="/reservations">Reservations</Link>
      <Link to="/login" style={{ marginLeft: 'auto' }}>Login</Link>
    </nav>
    <main>{children}</main>
  </div>
);

const RequireAuth = ({ children }) => {
  if (!isAuthed()) return <Navigate to="/login" replace />;
  return children;
};

const Dashboard = () => (
  <div>
    <h1>Transpo Dashboard</h1>
    <p>Welcome to Transpo. Use the navigation to explore.</p>
  </div>
);

export default function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/buses" element={<RequireAuth><BusesPage /></RequireAuth>} />
          <Route path="/routes" element={<RequireAuth><RoutesPage /></RequireAuth>} />
          <Route path="/schedules" element={<RequireAuth><SchedulesPage /></RequireAuth>} />
          <Route path="/reservations" element={<RequireAuth><ReservationsPage /></RequireAuth>} />
          <Route path="/map" element={<RequireAuth><MapPage /></RequireAuth>} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

// Lazy simple pages
import { useState } from 'react';
import { login, register } from './api/auth';
import { getBuses, getRoutes, getSchedules, getReservations, createReservation, getMapData, getReservationsByUser } from './api/resources';

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
      window.location.href = '/';
    } catch (err) {
      setError(err?.response?.data?.message || 'Login failed');
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
  useState(() => {
    (async () => {
      try {
        const data = await getBuses();
        setItems(Array.isArray(data) ? data : (data?.content ?? []));
      } catch (err) {
        setError(err?.response?.data?.message || 'Failed to load buses');
      } finally {
        setLoading(false);
      }
    })();
  }, []);
  if (loading) return <p>Loading...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;
  return (
    <div>
      <h2>Buses</h2>
      <DataTable items={items} />
    </div>
  );
}

function RoutesPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useState(() => {
    (async () => {
      try {
        const data = await getRoutes();
        setItems(Array.isArray(data) ? data : (data?.content ?? []));
      } catch (err) {
        setError(err?.response?.data?.message || 'Failed to load routes');
      } finally {
        setLoading(false);
      }
    })();
  }, []);
  if (loading) return <p>Loading...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;
  return (
    <div>
      <h2>Routes</h2>
      <DataTable items={items} />
    </div>
  );
}

function SchedulesPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useState(() => { (async () => {
    try {
      const data = await getSchedules();
      setItems(Array.isArray(data) ? data : (data?.content ?? []));
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load schedules');
    } finally {
      setLoading(false);
    }
  })(); }, []);

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
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(280px,1fr))', gap: '1rem' }}>
        {Object.entries(groups).map(([route, list]) => (
          <div key={route} style={{ border: '1px solid #ddd', borderRadius: 8, padding: '0.75rem' }}>
            <h3 style={{ margin: '0 0 .5rem' }}>{route}</h3>
            {list.map((s) => (
              <div key={s.id} style={{ borderTop: '1px dashed #eee', padding: '.5rem 0' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <strong>{s.departureTime || s.time || '—'}</strong>
                  <span>{s.arrivalTime ? `→ ${s.arrivalTime}` : ''}</span>
                </div>
                <div style={{ fontSize: '.9rem', color: '#555' }}>
                  Bus: {s.busNumber || s.bus?.number || 'N/A'} · Stop: {s.stopName || s.stop?.name || 'N/A'}
                </div>
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

function ReservationsPage() {
  const [items, setItems] = useState([]);
  const [mine, setMine] = useState([]);
  const [scheduleId, setScheduleId] = useState('');
  const [passengerName, setPassengerName] = useState('');
  const [passengerEmail, setPassengerEmail] = useState('');
  const [seatNumber, setSeatNumber] = useState(1);
  const [pickupStopId, setPickupStopId] = useState('');
  const [dropStopId, setDropStopId] = useState('');
  useState(() => { (async () => {
    setItems(await getReservations());
    const me = localStorage.getItem('token');
    if (me) {
      try { setMine(await getReservationsByUser(me)); } catch {}
    }
  })(); }, []);
  const onCreate = async (e) => {
    e.preventDefault();
    await createReservation({
      scheduleId: Number(scheduleId),
      passengerName,
      passengerEmail,
      seatNumber: Number(seatNumber),
      pickupStopId: pickupStopId ? Number(pickupStopId) : undefined,
      dropStopId: dropStopId ? Number(dropStopId) : undefined,
    });
    setItems(await getReservations());
    const me = localStorage.getItem('token');
    if (me) { try { setMine(await getReservationsByUser(me)); } catch {} }
  };
  return (
    <div>
      <h2>Reservations</h2>
      <form onSubmit={onCreate} className="form" style={{ flexWrap: 'wrap' }}>
        <input placeholder="Schedule ID" value={scheduleId} onChange={(e) => setScheduleId(e.target.value)} />
        <input placeholder="Passenger Name" value={passengerName} onChange={(e) => setPassengerName(e.target.value)} />
        <input placeholder="Passenger Email" value={passengerEmail} onChange={(e) => setPassengerEmail(e.target.value)} />
        <input placeholder="Seat Number" type="number" value={seatNumber} onChange={(e) => setSeatNumber(e.target.value)} />
        <input placeholder="Pickup Stop ID (optional)" value={pickupStopId} onChange={(e) => setPickupStopId(e.target.value)} />
        <input placeholder="Drop Stop ID (optional)" value={dropStopId} onChange={(e) => setDropStopId(e.target.value)} />
        <button type="submit">Create</button>
      </form>
      <ul>
        {items.map((r) => (<li key={r.id}>{r.status || JSON.stringify(r)}</li>))}
      </ul>
      <h3>My Reservations</h3>
      {mine && mine.length > 0 ? (
        <DataTable items={mine} />
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
