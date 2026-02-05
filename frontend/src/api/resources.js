import client from './client';

export const getBuses = () => client.get('/api/buses').then(r => r.data);
export const createBus = (bus) => client.post('/api/buses', bus).then(r => r.data);
export const getRoutes = () => client.get('/api/routes').then(r => r.data);
export const getSchedules = () => client.get('/api/schedules').then(r => r.data);
export const getReservations = () => client.get('/api/reservations').then(r => r.data);
export const createReservation = (payload) => client.post('/api/reservations/book', payload).then(r => r.data);
export const getMapData = () => client.get('/api/map').then(r => r.data);
