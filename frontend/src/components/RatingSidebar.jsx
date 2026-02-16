import React, { useEffect, useState } from 'react';

export default function RatingSidebar() {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');
  const [ratings, setRatings] = useState([]);

  async function loadRatings() {
    try {
      const res = await fetch('/api/ratings');
      let data = null; try { data = await res.json(); } catch { data = []; }
      setRatings(Array.isArray(data) ? data : []);
    } catch {}
  }

  useEffect(() => { loadRatings(); }, []);

  async function submit() {
    setErr(''); setMsg('');
    if (rating < 1 || rating > 5) { setErr('Rating must be between 1 and 5'); return; }
    setSubmitting(true);
    try {
      const res = await fetch('/api/ratings', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rating, comment })
      });
      let data = null; try { data = await res.json(); } catch {}
      if (!res.ok || (data && data.error)) throw new Error((data && data.error) || `Failed (${res.status})`);
      setMsg('Thanks for your rating!');
      setRating(0); setComment('');
      loadRatings();
    } catch (e) { setErr(e.message); }
    finally { setSubmitting(false); }
  }

  return (
    <aside style={{ minWidth: 280, maxWidth: 340, borderLeft: '1px solid #ddd', padding: '1rem' }}>
      <h3 style={{ marginTop: 0 }}>Rate This Application</h3>
      {msg && <div style={{ color: 'green', marginBottom: '.5rem' }}>{msg}</div>}
      {err && <div style={{ color: 'red', marginBottom: '.5rem' }}>{err}</div>}
      <div style={{ display: 'flex', gap: 4, marginBottom: '.5rem' }}>
        {[1,2,3,4,5].map(n => (
          <button key={n}
            onClick={() => setRating(n)}
            style={{
              cursor: 'pointer',
              background: 'transparent',
              border: 'none',
              fontSize: 22,
              color: n <= rating ? '#ffb400' : '#999'
            }}>★</button>
        ))}
      </div>
      <textarea
        placeholder="Optional comment"
        value={comment}
        onChange={e => setComment(e.target.value)}
        style={{ width: '100%', minHeight: 80, marginBottom: '.5rem' }}
      />
      <button onClick={submit} disabled={submitting} style={{ padding: '.5rem 1rem' }}>
        {submitting ? 'Submitting…' : 'Submit'}
      </button>

      <hr style={{ margin: '1rem 0' }} />
      <h4 style={{ marginTop: 0 }}>Recent Ratings</h4>
      <div style={{ display: 'grid', gap: '.5rem' }}>
        {(ratings || []).slice(0, 10).map(r => (
          <div key={r.id} style={{ border: '1px solid #eee', padding: '.5rem', borderRadius: 6 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              {r.profileImageUrl ? (
                <img src={r.profileImageUrl} alt={r.username} style={{ width: 28, height: 28, borderRadius: '50%', objectFit: 'cover' }} />
              ) : (
                <div style={{ width: 28, height: 28, borderRadius: '50%', background: '#ccc' }} />
              )}
              <strong>{r.username}</strong>
              <span style={{ marginLeft: 'auto', color: '#ffb400' }}>
                {'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}
              </span>
            </div>
            {r.comment && <div style={{ marginTop: 4 }}>{r.comment}</div>}
            {r.createdAt && <div style={{ fontSize: 12, color: '#666', marginTop: 4 }}>{new Date(r.createdAt).toLocaleString()}</div>}
          </div>
        ))}
      </div>
    </aside>
  );
}