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
  const [panel, setPanel] = useState({ loading: false, error: '', info: null, stateMsg: '' });

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

  const onSeatClick = async (s) => {
    if (loading) return;
    // Driver cannot click; Passenger can click only AVAILABLE or their own reserved seat; Admin/Conductor full manage.
    const mySeat = s && data && Array.isArray(data.seats) && data.seats.find(x => x.seatNumber === s.seatNumber);
    const isMine = mySeat && username && mySeat.passengerName && mySeat.passengerName.length > 0; // passengerName provided only for own seat or admin/conductor

    const allowedPassengerClick = role === 'PASSENGER' && (s.status === 'AVAILABLE' || isMine);
    if (readOnly || (!canManage && !allowedPassengerClick)) return;
    setSelectedSeat(s.seatNumber === selectedSeat ? null : s.seatNumber);
    // Conductor: load right panel seat details
    if (role === 'CONDUCTOR' && scheduleId && s) {
      try {
        setPanel(p => ({ ...p, loading: true, error: '', stateMsg: '' }));
        const res = await fetch(`/api/reservations/seat?scheduleId=${encodeURIComponent(scheduleId)}&seatNumber=${encodeURIComponent(s.seatNumber)}`);
        const info = await res.json();
        setPanel(p => ({ ...p, loading: false, info }));
      } catch (e) {
        setPanel(p => ({ ...p, loading: false, error: 'Failed to load seat details' }));
      }
    } else {
      setPanel({ loading: false, error: '', info: null, stateMsg: '' });
    }
  };

  const seatStyle = (s) => {
    const base = {
      width: 48, height: 48,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      borderRadius: 6, cursor: 'pointer', border: '1px solid #0001',
      fontSize: 12, position: 'relative'
    };
    let bg = COLORS.AVAILABLE;
    // Prefer manual state if present
    const state = s.state || s.status;
    if (state === 'RESERVED') bg = COLORS.RESERVED;
    if (state === 'PAID') bg = COLORS.PAID;
    if (state === 'DISABLED') bg = COLORS.DISABLED;
    if (selectedSeat === s.seatNumber) bg = COLORS.SELECTED;
    return { ...base, background: bg, color: '#fff' };
  };

  return (
    <div style={{ display: 'grid', gridTemplateColumns: role === 'CONDUCTOR' ? '1fr 300px' : '1fr', gap: 12 }}>
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
            <div key={s.seatNumber} style={seatStyle(s)} onClick={() => onSeatClick(s)} title={`${'Seat ' + s.seatNumber} | ${(s.state || s.status)}${(s.passengerName && (role === 'ADMIN' || role === 'CONDUCTOR')) ? ' | ' + s.passengerName : ''}`}>
              {s.seatNumber}
            </div>
          ))}
        </div>
      )}
      {role === 'CONDUCTOR' && (
        <div style={{ borderLeft: '1px solid #eee', paddingLeft: 12 }}>
          <h4>Seat Details</h4>
          {!selectedSeat && <div>Select a seat to view details</div>}
          {selectedSeat && panel.loading && <div>Loading seat details…</div>}
          {selectedSeat && panel.error && <div style={{ color: 'red' }}>{panel.error}</div>}
          {selectedSeat && !panel.loading && panel.info && (
            <div style={{ display: 'grid', gap: 8 }}>
              <div><strong>Seat:</strong> {panel.info.seatNumber}</div>
              <div><strong>Schedule:</strong> {panel.info.scheduleId}</div>
              {panel.info.reserved ? (
                <>
                  <div><strong>Reservation ID:</strong> {panel.info.reservationId}</div>
                  <div><strong>Passenger:</strong> {panel.info.passengerName}</div>
                  <div><strong>Payment:</strong> {panel.info.paid ? 'Paid' : 'Unpaid'}</div>
                </>
              ) : (
                <div style={{ color: '#555' }}>Not Reserved</div>
              )}
              <div><strong>Manual State:</strong> {panel.info.state || '—'}</div>
              <div style={{ display: 'grid', gap: 6 }}>
                <label>Change State</label>
                <select onChange={e => setPanel(p => ({ ...p, nextState: e.target.value }))} defaultValue="">
                  <option value="">Select state…</option>
                  <option value="AVAILABLE">Available</option>
                  <option value="RESERVED">Reserved</option>
                  <option value="PAID">Paid</option>
                  <option value="DISABLED">Disabled</option>
                </select>
                <button disabled={!panel.nextState} onClick={async () => {
                  // Optimistic UI update: reflect color change immediately
                  const next = panel.nextState;
                  const seatNo = selectedSeat;
                  let previousState;
                  try {
                    setPanel(p => ({ ...p, stateMsg: '', error: '' }));
                    // Update local grid state
                    setData(prev => {
                      if (!prev) return prev;
                      const seats = (prev.seats || []).map(s => {
                        if (s.seatNumber === seatNo) {
                          previousState = s.state || s.status;
                          return { ...s, state: next };
                        }
                        return s;
                      });
                      return { ...prev, seats };
                    });

                    // Persist to backend
                    const res = await fetch(`/api/reservations/seat/state?scheduleId=${encodeURIComponent(scheduleId)}&seatNumber=${encodeURIComponent(seatNo)}&state=${encodeURIComponent(next)}`, { method: 'PUT' });
                    if (!res.ok) throw new Error('Update failed');

                    // Refresh right panel info only (no full grid fetch)
                    const dres = await fetch(`/api/reservations/seat?scheduleId=${encodeURIComponent(scheduleId)}&seatNumber=${encodeURIComponent(seatNo)}`);
                    const info = await dres.json();
                    setPanel(p => ({ ...p, info, stateMsg: 'State updated' }));
                  } catch (e) {
                    // Roll back on failure
                    setData(prev => {
                      if (!prev) return prev;
                      const seats = (prev.seats || []).map(s => {
                        if (s.seatNumber === seatNo) {
                          const base = { ...s };
                          if (previousState) {
                            base.state = previousState;
                          }
                          return base;
                        }
                        return s;
                      });
                      return { ...prev, seats };
                    });
                    setPanel(p => ({ ...p, error: 'Failed to update state' }));
                  }
                }}>Apply</button>
                {panel.stateMsg && <div style={{ color: 'green' }}>{panel.stateMsg}</div>}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
