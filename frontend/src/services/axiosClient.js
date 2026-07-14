import axios from 'axios';

let isRedirecting = false;

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  withCredentials: true,
  // No default Content-Type — axios auto-detects:
  //   plain objects → application/json
  //   FormData      → multipart/form-data (with boundary)
});

axiosClient.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !isRedirecting) {
      isRedirecting = true;
      sessionStorage.removeItem('token');
      // Reset flag on navigation to prevent permanent lockout
      setTimeout(() => { isRedirecting = false; }, 2000);
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
