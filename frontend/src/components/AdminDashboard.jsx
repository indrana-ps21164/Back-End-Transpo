import React, { useEffect, useState } from 'react';
import { whoami } from '../api/auth';
import {
  getBuses, createBus, updateBus, deleteBus,
  getRoutes, createRoute, updateRoute, deleteRoute,
  getSchedules, createSchedule, updateSchedule, deleteSchedule,
  adminUpdateDriverAssignment, updateConductorAssignment,
  getAdminBuses,
} from '../api/resources';

export default function AdminDashboard() {
  const [me, setMe] = useState(null);
  const [active, setActive] = useState('bus');
  useEffect(() => { (async () => { try { setMe(await whoami()); } catch {} })(); }, []);
  if (!me) return <p>Loading...</p>;
  if (me.role !== 'ADMIN') return <p>Access Denied</p>;
  return (
    <div>
      <h1>Admin Dashboard</h1>
      <div style={{ display: 'flex', gap: '.5rem', marginBottom: '1rem' }}>
        <button onClick={() => setActive('bus')}>Bus</button>
        <button onClick={() => setActive('route')}>Route</button>
        <button onClick={() => setActive('schedule')}>Schedule</button>
        <button onClick={() => setActive('assignments')}>Assignments</button>
      </div>
      {active === 'bus' && <BusManager />}
      {active === 'route' && <RouteManager />}
      {active === 'schedule' && <ScheduleManager />}
      {active === 'assignments' && <AssignmentManager />}
    </div>
  );
}

function BusManager() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [form, setForm] = useState({ id: null, busName: '', busNumber: '', capacity: 40 });
  useEffect(() => { (async () => { try { setItems(await getAdminBuses()); } catch (e) { setError('Failed to load buses'); } })(); }, []);
  const handleCreateBus = async () => {
    setError(''); setSuccess('');
    if (!form.busName || !form.busNumber) { setError('Bus name and number required'); return; }
    try { await createBus({ busName: form.busName, busNumber: form.busNumber, capacity: Number(form.capacity) }); setItems(await getBuses()); setSuccess('Bus created'); setForm({ id: null, busName: '', busNumber: '', capacity: 40 }); } catch (e) { setError(e?.response?.data?.message || 'Create failed'); }
  };
  const handleEditBus = async (item) => { setForm({ id: item.id, busName: item.busName || '', busNumber: item.busNumber || '', capacity: item.capacity || 40 }); };
  const handleUpdateBus = async () => {
    setError(''); setSuccess('');
    if (!form.id) { setError('No bus selected'); return; }
    try { await updateBus(form.id, { busName: form.busName, busNumber: form.busNumber, capacity: Number(form.capacity) }); setItems(await getBuses()); setSuccess('Bus updated'); setForm({ id: null, busName: '', busNumber: '', capacity: 40 }); } catch (e) { setError(e?.response?.data?.message || 'Update failed'); }
  };
  const handleDeleteBus = async (id) => {
    if (!window.confirm('Delete this bus?')) return;
    setError(''); setSuccess('');
    try { await deleteBus(id); setItems(await getBuses()); setSuccess('Bus deleted'); } catch (e) { setError(e?.response?.data?.message || 'Delete failed'); }
  };
  return (
    <div>
      <h2>Bus Management</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {success && <p style={{ color: 'green' }}>{success}</p>}
      <div className="form" style={{ gap: '.5rem', marginBottom: '.75rem' }}>
        <input placeholder="Bus Name" value={form.busName} onChange={(e) => setForm({ ...form, busName: e.target.value })} />
        <input placeholder="Bus Number" value={form.busNumber} onChange={(e) => setForm({ ...form, busNumber: e.target.value })} />
        <input type="number" placeholder="Capacity" value={form.capacity} onChange={(e) => setForm({ ...form, capacity: e.target.value })} />
        <button onClick={handleCreateBus}>Create</button>
        <button onClick={handleUpdateBus}>Update</button>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(260px,1fr))', gap: '1rem' }}>
        {items?.map((b) => (
          <div key={b.id} style={{ border: '1px solid #ddd', borderRadius: 8, padding: '.75rem' }}>
            <div><strong>{b.busNumber}</strong> — {b.busName}</div>
            <div style={{ fontSize: '.85rem', color: '#555' }}>Capacity: {b.capacity ?? '—'}</div>
            <div style={{ marginTop: '.25rem', fontSize: '.85rem', color: '#333' }}>
              <div>Driver: {b.driverUsername || 'Not Assigned'}</div>
              <div>Conductor: {b.conductorUsername || 'Not Assigned'}</div>
            </div>
            <div style={{ display: 'flex', gap: '.5rem', marginTop: '.5rem' }}>
              <button onClick={() => handleEditBus(b)}>Edit</button>
              <button onClick={() => handleDeleteBus(b.id)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function AssignmentManager() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [driverForm, setDriverForm] = useState({ username: '', busId: '' });
  const [conductorForm, setConductorForm] = useState({ username: '', busId: '' });
  const [conductorUpdateAvailable, setConductorUpdateAvailable] = useState(true);
  const [busNumberFilter, setBusNumberFilter] = useState('');
  const [currentDriver, setCurrentDriver] = useState('');
  const [currentConductor, setCurrentConductor] = useState('');
  const [editDriver, setEditDriver] = useState('');
  const [editConductor, setEditConductor] = useState('');

  // Load current driver/conductor by bus number
  const loadAssignmentsForBus = async () => {
    setLoading(true);
    setError(''); setSuccess('');
    setCurrentDriver(''); setCurrentConductor('');
    try {
      const buses = await getAdminBuses();
      const match = (Array.isArray(buses) ? buses : []).find(b => String(b.busNumber).trim().toLowerCase() === String(busNumberFilter).trim().toLowerCase());
      if (!match) {
        setError('Bus not found');
      } else {
        setCurrentDriver(match.driverUsername || 'Not Assigned');
        setCurrentConductor(match.conductorUsername || 'Not Assigned');
  setEditDriver(match.driverUsername || '');
  setEditConductor(match.conductorUsername || '');
        // Prefill busId for forms if needed (requires id)
        if (match.id) {
          setDriverForm(prev => ({ ...prev, busId: String(match.id) }));
          setConductorForm(prev => ({ ...prev, busId: String(match.id) }));
        }
      }
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to load bus assignments');
    } finally {
      setLoading(false);
    }
  };

  const refreshLists = async () => { await loadAssignmentsForBus(); };

  const saveFilteredDriver = async () => {
    setError(''); setSuccess('');
    if (!editDriver || !driverForm.busId) { setError('Driver username and Bus ID required'); return; }
    try {
      await adminUpdateDriverAssignment({ username: editDriver, busId: Number(driverForm.busId) });
      setSuccess('Filtered bus driver updated');
      await loadAssignmentsForBus();
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to update filtered driver');
    }
  };

  const saveFilteredConductor = async () => {
    setError(''); setSuccess('');
    if (!editConductor || !conductorForm.busId) { setError('Conductor username and Bus ID required'); return; }
    try {
      await updateConductorAssignment(editConductor, Number(conductorForm.busId));
      setSuccess('Filtered bus conductor updated');
      await loadAssignmentsForBus();
    } catch (e) {
      const status = e?.response?.status;
      if (status === 404) {
        setConductorUpdateAvailable(false);
        setError('No assignment API available for conductor (404).');
      } else {
        setError(e?.response?.data?.message || 'Failed to update filtered conductor');
      }
    }
  };

  const handleChangeDriverBus = async () => {
    setError(''); setSuccess('');
    if (!driverForm.username || !driverForm.busId) { setError('Driver username and Bus ID required'); return; }
    try {
      await adminUpdateDriverAssignment({ username: driverForm.username, busId: Number(driverForm.busId) });
      setSuccess('Driver bus assignment updated');
      setDriverForm({ username: '', busId: '' });
  await refreshLists();
    } catch (e) {
      const status = e?.response?.status;
      if (status === 404) {
        setError('Driver assignment API not found (404). Please check backend route.');
      } else if (status === 401 || status === 403) {
        setError('Not authorized. Admin role required for driver assignment.');
      } else {
        setError(e?.response?.data?.message || 'Failed to update driver assignment');
      }
    }
  };

  const handleRemoveDriver = async (username) => {
    setError(''); setSuccess('');
    try {
      await adminRemoveDriverAssignment({ username });
      setSuccess('Driver unassigned from bus');
      await refreshLists();
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to unassign driver');
    }
  };

  const handleChangeConductorBus = async () => {
    setError(''); setSuccess('');
    if (!conductorForm.username || !conductorForm.busId) { setError('Conductor username and Bus ID required'); return; }
    try {
  await updateConductorAssignment(conductorForm.username, Number(conductorForm.busId));
      setSuccess('Conductor bus assignment updated');
      setConductorForm({ username: '', busId: '' });
  await refreshLists();
    } catch (e) {
      const status = e?.response?.status;
      if (status === 404) {
        setConductorUpdateAvailable(false);
        setError('No assignment API available for conductor (404).');
      } else {
        setError(e?.response?.data?.message || 'Failed to update conductor assignment');
      }
    }
  };

  const handleRemoveConductor = async (username) => {
    setError(''); setSuccess('');
    try {
      await adminRemoveConductorAssignment({ username });
      setSuccess('Conductor unassigned from bus');
      await refreshLists();
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to unassign conductor');
    }
  };

  return (
    <div>
      <h2>Assignments</h2>
      {loading && <p>Loading assignments...</p>}
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {success && <p style={{ color: 'green' }}>{success}</p>}
      <div className="form" style={{ gap: '.5rem', marginBottom: '.75rem' }}>
        <input placeholder="Filter by Bus Number" value={busNumberFilter} onChange={(e) => setBusNumberFilter(e.target.value)} />
        <button onClick={loadAssignmentsForBus}>Load Assignments</button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', alignItems: 'start', marginBottom: '1rem' }}>
        <div>
          <h3>Current Driver</h3>
          <p style={{ marginTop: '.25rem' }}>{currentDriver || '—'}</p>
          <div className="form" style={{ gap: '.5rem', marginTop: '.5rem' }}>
            <input placeholder="Edit Driver Username" value={editDriver} onChange={(e) => setEditDriver(e.target.value)} />
            <button onClick={saveFilteredDriver}>Save Driver</button>
          </div>
        </div>
        <div>
          <h3>Current Conductor</h3>
          <p style={{ marginTop: '.25rem' }}>{currentConductor || '—'}</p>
          <div className="form" style={{ gap: '.5rem', marginTop: '.5rem' }}>
            <input placeholder="Edit Conductor Username" value={editConductor} onChange={(e) => setEditConductor(e.target.value)} />
            <button onClick={saveFilteredConductor} disabled={!conductorUpdateAvailable}>Save Conductor</button>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', alignItems: 'start' }}>
        <div>
          <h3>Drivers</h3>
          <div className="form" style={{ gap: '.5rem', marginBottom: '.75rem' }}>
            <input placeholder="Driver Username" value={driverForm.username} onChange={(e) => setDriverForm({ ...driverForm, username: e.target.value })} />
            <input type="number" placeholder="Bus ID" value={driverForm.busId} onChange={(e) => setDriverForm({ ...driverForm, busId: e.target.value })} />
            <button onClick={handleChangeDriverBus}>Change Driver Bus</button>
          </div>
          {/* List removed per request */}
        </div>

        <div>
          <h3>Conductors</h3>
          <div className="form" style={{ gap: '.5rem', marginBottom: '.75rem' }}>
            <input placeholder="Conductor Username" value={conductorForm.username} onChange={(e) => setConductorForm({ ...conductorForm, username: e.target.value })} />
            <input type="number" placeholder="Bus ID" value={conductorForm.busId} onChange={(e) => setConductorForm({ ...conductorForm, busId: e.target.value })} />
            <button onClick={handleChangeConductorBus} disabled={!conductorUpdateAvailable}>Change Conductor Bus</button>
            {!conductorUpdateAvailable && (
              <span style={{ color: '#b00', fontSize: '.85rem' }}>No assignment API available.</span>
            )}
          </div>
          {/* List removed per request */}
        </div>
      </div>
    </div>
  );
}

function RouteManager() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [form, setForm] = useState({ id: null, name: '', origin: '', destination: '', stops: '' });
  useEffect(() => { (async () => { try { setItems(await getRoutes()); } catch (e) { setError('Failed to load routes'); } })(); }, []);
  const handleCreateRoute = async () => {
    setError(''); setSuccess('');
    if (!form.origin || !form.destination) { setError('Origin and destination required'); return; }
    try {
      const stopsArr = (form.stops || '').split(',').map(s => s.trim()).filter(Boolean);
      await createRoute({ name: form.name, origin: form.origin, destination: form.destination, stops: stopsArr });
      setItems(await getRoutes()); setSuccess('Route created'); setForm({ id: null, name: '', origin: '', destination: '', stops: '' });
    } catch (e) { setError(e?.response?.data?.message || 'Create failed'); }
  };
  const handleEditRoute = (item) => {
    const stopsStr = (item.stops || []).join(', ');
    setForm({ id: item.id, name: item.name || '', origin: item.origin || '', destination: item.destination || '', stops: stopsStr });
  };
  const handleUpdateRoute = async () => {
    setError(''); setSuccess('');
    if (!form.id) { setError('No route selected'); return; }
    try {
      const stopsArr = (form.stops || '').split(',').map(s => s.trim()).filter(Boolean);
      await updateRoute(form.id, { name: form.name, origin: form.origin, destination: form.destination, stops: stopsArr });
      setItems(await getRoutes()); setSuccess('Route updated'); setForm({ id: null, name: '', origin: '', destination: '', stops: '' });
    } catch (e) { setError(e?.response?.data?.message || 'Update failed'); }
  };
  const handleDeleteRoute = async (id) => {
    if (!window.confirm('Delete this route?')) return;
    setError(''); setSuccess('');
    try { await deleteRoute(id); setItems(await getRoutes()); setSuccess('Route deleted'); } catch (e) { setError(e?.response?.data?.message || 'Delete failed'); }
  };
  return (
    <div>
      <h2>Route Management</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {success && <p style={{ color: 'green' }}>{success}</p>}
      <div className="form" style={{ gap: '.5rem', marginBottom: '.75rem' }}>
        <input placeholder="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <input placeholder="Origin" value={form.origin} onChange={(e) => setForm({ ...form, origin: e.target.value })} />
        <input placeholder="Destination" value={form.destination} onChange={(e) => setForm({ ...form, destination: e.target.value })} />
        <input placeholder="Stops (comma-separated)" value={form.stops} onChange={(e) => setForm({ ...form, stops: e.target.value })} />
        <button onClick={handleCreateRoute}>Create</button>
        <button onClick={handleUpdateRoute}>Update</button>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(260px,1fr))', gap: '1rem' }}>
        {items?.map((r) => (
          <div key={r.id} style={{ border: '1px solid #ddd', borderRadius: 8, padding: '.75rem' }}>
            <div><strong>{r.name || (r.origin + ' → ' + r.destination)}</strong></div>
            <div style={{ fontSize: '.85rem', color: '#555' }}>Stops: {(r.stops || []).join(', ')}</div>
            <div style={{ display: 'flex', gap: '.5rem', marginTop: '.5rem' }}>
              <button onClick={() => handleEditRoute(r)}>Edit</button>
              <button onClick={() => handleDeleteRoute(r.id)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function ScheduleManager() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [form, setForm] = useState({ id: null, busId: '', routeId: '', departureTime: '', arrivalTime: '' });
  useEffect(() => { (async () => { try { setItems(await getSchedules()); } catch (e) { setError('Failed to load schedules'); } })(); }, []);
  const handleCreateSchedule = async () => {
    setError(''); setSuccess('');
    if (!form.busId || !form.routeId || !form.departureTime) { setError('Bus, route, and departure time required'); return; }
    try { await createSchedule({ busId: Number(form.busId), routeId: Number(form.routeId), departureTime: form.departureTime, arrivalTime: form.arrivalTime }); setItems(await getSchedules()); setSuccess('Schedule created'); setForm({ id: null, busId: '', routeId: '', departureTime: '', arrivalTime: '' }); } catch (e) { setError(e?.response?.data?.message || 'Create failed'); }
  };
  const handleEditSchedule = (item) => {
    setForm({ id: item.id, busId: item.busId || item.bus?.id || '', routeId: item.routeId || item.route?.id || '', departureTime: item.departureTime || '', arrivalTime: item.arrivalTime || '' });
  };
  const handleUpdateSchedule = async () => {
    setError(''); setSuccess('');
    if (!form.id) { setError('No schedule selected'); return; }
    try { await updateSchedule(form.id, { busId: Number(form.busId), routeId: Number(form.routeId), departureTime: form.departureTime, arrivalTime: form.arrivalTime }); setItems(await getSchedules()); setSuccess('Schedule updated'); setForm({ id: null, busId: '', routeId: '', departureTime: '', arrivalTime: '' }); } catch (e) { setError(e?.response?.data?.message || 'Update failed'); }
  };
  const handleDeleteSchedule = async (id) => {
    if (!window.confirm('Delete this schedule?')) return;
    setError(''); setSuccess('');
    try { await deleteSchedule(id); setItems(await getSchedules()); setSuccess('Schedule deleted'); } catch (e) { setError(e?.response?.data?.message || 'Delete failed'); }
  };
  return (
    <div>
      <h2>Schedule Management</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {success && <p style={{ color: 'green' }}>{success}</p>}
      <div className="form" style={{ gap: '.5rem', marginBottom: '.75rem' }}>
        <input placeholder="Bus ID" value={form.busId} onChange={(e) => setForm({ ...form, busId: e.target.value })} />
        <input placeholder="Route ID" value={form.routeId} onChange={(e) => setForm({ ...form, routeId: e.target.value })} />
        <input type="datetime-local" placeholder="Departure" value={form.departureTime} onChange={(e) => setForm({ ...form, departureTime: e.target.value })} />
        <input type="datetime-local" placeholder="Arrival" value={form.arrivalTime} onChange={(e) => setForm({ ...form, arrivalTime: e.target.value })} />
        <button onClick={handleCreateSchedule}>Create</button>
        <button onClick={handleUpdateSchedule}>Update</button>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(260px,1fr))', gap: '1rem' }}>
        {items?.map((s) => (
          <div key={s.id} style={{ border: '1px solid #ddd', borderRadius: 8, padding: '.75rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <strong>#{s.id}</strong>
              <span>{s.departureTime || '—'}</span>
            </div>
            <div style={{ fontSize: '.85rem', color: '#555' }}>Bus: {s.busNumber || s.bus?.busNumber || 'N/A'} · Route: {s.origin || s.route?.origin} → {s.destination || s.route?.destination}</div>
            <div style={{ display: 'flex', gap: '.5rem', marginTop: '.5rem' }}>
              <button onClick={() => handleEditSchedule(s)}>Edit</button>
              <button onClick={() => handleDeleteSchedule(s.id)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
