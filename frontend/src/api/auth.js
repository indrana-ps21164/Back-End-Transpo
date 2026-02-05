import client from './client';

export const login = async (username, password) => {
  const { data } = await client.post('/auth/login', { username, password });
  return data;
};

export const register = async ({ username, password, role }) => {
  const normalized = role ? String(role).toUpperCase() : '';
  const config = normalized ? { params: { role: normalized } } : {};
  const { data } = await client.post('/auth/register', { username, password }, config);
  return data;
};
