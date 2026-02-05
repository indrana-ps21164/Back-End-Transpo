import client from './client';

export const getBuses = () => client.get('/buses').then(r => r.data);
export const createBus = (bus) => client.post('/buses', bus).then(r => r.data);
export const getRoutes = () => client.get('/routes').then(r => r.data);
export const getSchedules = () => client.get('/schedules').then(r => r.data);
export const getReservations = () => client.get('/reservations').then(r => r.data);
export const createReservation = (payload) => client.post('/reservations/book', payload).then(r => r.data);
export const getMapData = () => client.get('/map').then(r => r.data);
