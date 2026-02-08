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
import { whoami } from './api/auth';

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
        try {
          const me = await whoami();
          setIsAdmin(me?.role === 'ADMIN');
        } catch {}
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
                {isAdmin && (
                  <div className="form" style={{ marginTop: '.5rem', flexWrap: 'wrap' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                      <label><strong>Bus ID</strong></label>
                      <input
                        value={s.busId || ''}
                        onChange={(e) => handleScheduleChange(s.id, 'busId', e.target.value)}
                      />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                      <label><strong>Departure Time</strong></label>
                      <input
                        value={s.departureTime || ''}
                        onChange={(e) => handleScheduleChange(s.id, 'departureTime', e.target.value)}
                      />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', marginRight: '0.5rem' }}>
                      <label><strong>Route ID</strong></label>
                      <input
                        value={s.routeId || ''}
                        onChange={(e) => handleScheduleChange(s.id, 'routeId', e.target.value)}
                      />
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end' }}>
                      <button
                        onClick={async () => {
                          await updateSchedule(s.id, s);
                          await refreshSchedules();
                        }}
                      >
                        Save
                      </button>
                      <button
                        onClick={async () => {
                          await deleteSchedule(s.id);
                          await refreshSchedules();
                        }}
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                )}
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
  const [isAdmin, setIsAdmin] = useState(false);
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
  try { const who = await whoami(); setIsAdmin(who?.role === 'ADMIN'); } catch {}
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
      {isAdmin && (
        <div style={{ marginTop: '1rem' }}>
          <h3>Admin: Delete</h3>
          {items.map((r) => (
            <div key={r.id} className="form">
              <button onClick={async () => { await deleteReservation(r.id); setItems(await getReservations()); }}>Delete</button>
            </div>
          ))}
        </div>
      )}
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
