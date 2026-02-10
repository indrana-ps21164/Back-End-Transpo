import React, { useEffect, useState } from 'react';
import { getRoutesWithStops, searchSchedules } from '../api/resources';

export default function PassengerDashboard() {
  return (
    <div>
      <h1>Transpo Dashboard</h1>
      <p>Welcome to Transpo. Use the navigation to explore.</p>
      <DashboardSearchAndMap />
    </div>
  );
}

function DashboardSearchAndMap() {
  const [pickup, setPickup] = useState('');
  const [drop, setDrop] = useState('');
  const [schedules, setSchedules] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => { (async () => {
    try {
      const rs = await getRoutesWithStops();
      setRoutes(Array.isArray(rs) ? rs : []);
    } catch (e) {
      setError('Failed to load routes');
    }
  })(); }, []);

  const handleSearchSchedules = async () => {
    setError(''); setLoading(true);
    try {
      const data = await searchSchedules(pickup, drop);
      setSchedules(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e?.response?.data?.message || 'Search failed');
    } finally { setLoading(false); }
  };

  return (
    <div>
      <div className="form" style={{ marginBottom: '1rem' }}>
        <input placeholder="Pickup point" value={pickup} onChange={(e) => setPickup(e.target.value)} />
        <input placeholder="Drop point" value={drop} onChange={(e) => setDrop(e.target.value)} />
        <button onClick={handleSearchSchedules} disabled={loading}>{loading ? 'Searching…' : 'Search'}</button>
      </div>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <DashboardMap routes={routes} schedules={schedules} pickup={pickup} drop={drop} />
      <ScheduleResults schedules={schedules} pickup={pickup} drop={drop} />
    </div>
  );
}

function DashboardMap({ routes, schedules, pickup, drop }) {
  const [mapEl, setMapEl] = useState(null);
  const colors = ['#1e88e5', '#43a047', '#e53935', '#8e24aa', '#fb8c00', '#00acc1'];

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

    const routeIds = new Set((schedules || []).map(s => s.routeId));
    const showRoutes = routeIds.size > 0 ? (routes || []).filter(r => routeIds.has(r.id)) : (routes || []);

    const bounds = [];
    showRoutes.forEach((r, idx) => {
      const color = colors[idx % colors.length];
      const latlngs = [];
      (r.stops || []).forEach((s) => {
        if (s.latitude != null && s.longitude != null) {
          const latlng = [s.latitude, s.longitude];
          latlngs.push(latlng);
          bounds.push(latlng);
          const marker = L.marker(latlng).addTo(map);
          let label = s.name || 'Stop';
          const p = (pickup || '').toLowerCase();
          const d = (drop || '').toLowerCase();
          const nameLower = (s.name || '').toLowerCase();
          if (p && nameLower.includes(p)) {
            marker.setIcon(L.divIcon({ className: 'pickup-marker', html: '<div style="color:#fff;background:#43a047;padding:4px 6px;border-radius:4px">Pickup</div>' }));
            label = `${label} (Pickup)`;
          }
          if (d && nameLower.includes(d)) {
            marker.setIcon(L.divIcon({ className: 'drop-marker', html: '<div style="color:#fff;background:#e53935;padding:4px 6px;border-radius:4px">Drop</div>' }));
            label = `${label} (Drop)`;
          }
          marker.bindPopup(`<b>${label}</b><br/>${latlng[0].toFixed(5)}, ${latlng[1].toFixed(5)}`);
        }
      });
      if (latlngs.length > 1) {
        L.polyline(latlngs, { color, weight: 4, opacity: 0.9 }).addTo(map);
      }
    });
    if (bounds.length > 0) {
      const b = L.latLngBounds(bounds);
      map.fitBounds(b, { padding: [20, 20] });
    } else {
      map.setView([6.9271, 79.8612], 11);
    }
  })(); }, [mapEl, routes, schedules, pickup, drop]);

  return (
    <div>
      <h3>Routes Map</h3>
      {(!routes || routes.length === 0) && (
        <p style={{ color: '#666' }}>No routes available.</p>
      )}
      <div ref={setMapEl} style={{ height: 420, border: '1px solid #ddd', borderRadius: 8 }} />
    </div>
  );
}

function ScheduleResults({ schedules, pickup, drop }) {
  const navigateToReservation = (s, pStop, dStop) => {
    const params = new URLSearchParams({ scheduleId: s.id, pickup: pStop || '', drop: dStop || '' });
    window.location.href = `/reservations?${params.toString()}`;
  };
  if (!schedules || schedules.length === 0) return <p>No schedules match your search.</p>;
  return (
    <div style={{ marginTop: '1rem' }}>
      <h3>Matching Schedules</h3>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(280px,1fr))', gap: '1rem' }}>
        {schedules.map((s) => (
          <div key={s.id} style={{ border: '1px solid #ddd', borderRadius: 8, padding: '0.75rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <strong>{s.departureTime || '—'}</strong>
              <span>Bus: {s.busNumber || 'N/A'}</span>
            </div>
            <div style={{ fontSize: '.9rem', color: '#333', marginTop: '.25rem' }}>
              <div>Route: {s.origin} → {s.destination}</div>
              <div>Pickup: {pickup}</div>
              <div>Drop: {drop}</div>
            </div>
            <div style={{ marginTop: '.5rem' }}>
              <button onClick={() => navigateToReservation(s, pickup, drop)}>Reserve</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
