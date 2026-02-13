import React, { useEffect, useState } from 'react';
import { whoami } from '../api/auth';

export default function ConductorDashboard() {
  const [me, setMe] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try { setMe(await whoami()); } finally { setLoading(false); }
    })();
  }, []);

  if (loading) return <p>Loading...</p>;
  if (!me || me.role !== 'CONDUCTOR') return <p>Access Denied</p>;

  return (
    <div>
      <h1>Seat Availability</h1>
      {/* Seat availability grid will be added later */}
      <p style={{ color: '#555' }}>This dashboard shows the bus seat availability for conductors.</p>
      <div style={{ marginTop: '1rem', padding: '1rem', border: '1px dashed #bbb', borderRadius: 8 }}>
        <strong>Placeholder:</strong> Seat grid will appear here.
      </div>
    </div>
  );
}
