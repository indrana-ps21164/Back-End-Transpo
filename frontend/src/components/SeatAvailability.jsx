import React, { useEffect, useMemo, useState } from 'react';
import { getSeatAvailability, payReservation, createReservation, deleteReservation } from '../api/resources';

// Color mapping
const COLORS = {
  AVAILABLE: '#22c55e', // green
  RESERVED: '#ef4444',  // red
  PAID: '#3b82f6',      // blue
  SELECTED: '#f59e0b',  // yellow
  DISABLED: '#9ca3af',  // gray
};

function Legend() {
  const items = [
    { label: 'Available', color: COLORS.AVAILABLE },
    { label: 'Reserved', color: COLORS.RESERVED },
    { label: 'Paid', color: COLORS.PAID },
    { label: 'Selected', color: COLORS.SELECTED },
    { label: 'Disabled', color: COLORS.DISABLED },
  ];
  return (
    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: 8 }}>
      {items.map(i => (
        <div key={i.label} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <span style={{ width: 16, height: 16, background: i.color, borderRadius: 3, border: '1px solid #0001' }} />
          <span style={{ fontSize: 12 }}>{i.label}</span>
        </div>
      ))}
    </div>
  );
}

export default function SeatAvailability({ busNumber, scheduleId, role, username }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedSeat, setSelectedSeat] = useState(null);

  const canManage = role === 'ADMIN' || role === 'CONDUCTOR';
  const readOnly = role === 'DRIVER';

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        setError('');
  const res = await getSeatAvailability(busNumber, scheduleId);
        setData(res);
      } catch (e) {
        setError(e?.response?.data?.message || e?.message || 'Failed to load seat availability');
      } finally { setLoading(false); }
    })();
  }, [busNumber, scheduleId]);

  const seats = useMemo(() => {
    if (!data) return [];
    // Ensure exactly totalSeats entries, fill missing with AVAILABLE
    const map = new Map();
    (data.seats || []).forEach(s => map.set(s.seatNumber, s));
    const arr = [];
    for (let i = 1; i <= (data.totalSeats || 0); i++) {
      arr.push(map.get(i) || { seatNumber: i, status: 'AVAILABLE' });
    }
    return arr;
  }, [data]);

  const onSeatClick = (s) => {
    if (loading) return;
    // Driver cannot click; Passenger can click only AVAILABLE or their own reserved seat; Admin/Conductor full manage.
    const mySeat = s && data && Array.isArray(data.seats) && data.seats.find(x => x.seatNumber === s.seatNumber);
    const isMine = mySeat && username && mySeat.passengerName && mySeat.passengerName.length > 0; // passengerName provided only for own seat or admin/conductor

    const allowedPassengerClick = role === 'PASSENGER' && (s.status === 'AVAILABLE' || isMine);
    if (readOnly || (!canManage && !allowedPassengerClick)) return;
    setSelectedSeat(s.seatNumber === selectedSeat ? null : s.seatNumber);
  };

  const seatStyle = (s) => {
    const base = {
      width: 48, height: 48,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      borderRadius: 6, cursor: 'pointer', border: '1px solid #0001',
      fontSize: 12, position: 'relative'
    };
    let bg = COLORS.AVAILABLE;
    if (s.status === 'RESERVED') bg = COLORS.RESERVED;
    if (s.status === 'PAID') bg = COLORS.PAID;
    if (selectedSeat === s.seatNumber) bg = COLORS.SELECTED;
    if (readOnly && s.status === 'AVAILABLE' && !canManage) bg = COLORS.DISABLED;
    return { ...base, background: bg, color: '#fff' };
  };

  return (
    <div>
      <h3>Seat Availability</h3>
      {data && (
        <div style={{ marginBottom: 8 }}>
          <strong>Bus:</strong> {data.busNumber} &nbsp; <strong>Schedule:</strong> {data.scheduleId}
        </div>
      )}
      <Legend />
      {error && <div style={{ color: 'red', marginBottom: 8 }}>{error}</div>}
      {loading && <div>Loading...</div>}
      {!loading && seats && seats.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 52px)', gap: 8 }}>
          {seats.map(s => (
            <div key={s.seatNumber} style={seatStyle(s)} onClick={() => onSeatClick(s)} title={`${'Seat ' + s.seatNumber} | ${s.status}${(s.passengerName && (role === 'ADMIN' || role === 'CONDUCTOR')) ? ' | ' + s.passengerName : ''}`}>
              {s.seatNumber}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
