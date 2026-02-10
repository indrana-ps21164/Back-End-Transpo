import client from './client';

// Bus APIs
export const getBuses = () => client.get('/api/buses').then(r => r.data);
export const createBus = (bus) => client.post('/api/buses', bus).then(r => r.data);
export const updateBus = (id, bus) => client.put(`/api/buses/${id}`, bus).then(r => r.data);
export const deleteBus = (id) => client.delete(`/api/buses/${id}`).then(r => r.data);

// Route APIs
export const getRoutes = () => client.get('/api/routes').then(r => r.data);
export const createRoute = (route) => client.post('/api/routes', route).then(r => r.data);
export const updateRoute = (id, route) => client.put(`/api/routes/${id}`, route).then(r => r.data);
export const deleteRoute = (id) => client.delete(`/api/routes/${id}`).then(r => r.data);

// Schedule APIs
export const getSchedules = () => client.get('/api/schedules').then(r => r.data);
export const createSchedule = (schedule) => client.post('/api/schedules', schedule).then(r => r.data);
export const updateSchedule = (id, schedule) => client.put(`/api/schedules/${id}`, schedule).then(r => r.data);
export const deleteSchedule = (id) => client.delete(`/api/schedules/${id}`).then(r => r.data);

// Reservation APIs
export const getReservations = () => client.get('/api/reservations').then(r => r.data);
// payload must include: scheduleId, passengerName, passengerEmail, seatNumber, optional pickupStopId, dropStopId
export const createReservation = (payload) =>
	client.post('/api/reservations/book', payload);

export const getReservationsByUser = (email) =>
	client.get('/api/reservations/by-email', { params: { email } }).then(r => r.data);

export const deleteReservation = (id) =>
	client.delete(`/api/reservations/${id}`).then(r => r.data);

export async function getMyReservations() {
  const res = await fetch('/api/reservations/me', {
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });
  if (!res.ok) throw new Error('Failed to load reservations');
  return res.json();
}

// Map API
export const getMapData = () => client.get('/api/map').then(r => r.data);
