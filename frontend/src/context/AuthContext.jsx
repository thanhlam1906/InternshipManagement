import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext(null);

// Session storage keys
const TOKEN_KEY = 'token';

// Helper: decode JWT payload without verification (for reading exp only)
function decodeJwtPayload(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

function isTokenExpired(token) {
  const payload = decodeJwtPayload(token);
  if (!payload || !payload.exp) return false;
  return Date.now() >= payload.exp * 1000;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const clearSession = useCallback(() => {
    sessionStorage.removeItem(TOKEN_KEY);
    setUser(null);
  }, []);

  // Check auth on mount
  useEffect(() => {
    const token = sessionStorage.getItem(TOKEN_KEY);
    if (!token) {
      setLoading(false);
      return;
    }

    // Check if token is expired before even calling /me
    if (isTokenExpired(token)) {
      clearSession();
      setLoading(false);
      return;
    }

    // Fetch current user from server
    authApi.me()
      .then(res => {
        if (res?.data?.data) {
          setUser(res.data.data);
        } else {
          clearSession();
        }
      })
      .catch(err => {
        // Only clear session on 401 (token invalid)
        if (err?.response?.status === 401) {
          clearSession();
        }
        // On network error, keep session alive — user stays logged in
        console.error('Failed to fetch current user:', err);
      })
      .finally(() => setLoading(false));
  }, [clearSession]);

  const login = useCallback(async (credentials) => {
    const res = await authApi.login(credentials);
    const { token, userId, username: uname, fullName, role } = res.data.data;
    sessionStorage.setItem(TOKEN_KEY, token);
    const userData = { userId, username: uname, fullName, role };
    setUser(userData);
    return userData;
  }, []);

  const register = useCallback(async (data) => {
    const res = await authApi.register(data);
    const { token, userId, username: uname, fullName, role } = res.data.data;
    sessionStorage.setItem(TOKEN_KEY, token);
    const userData = { userId, username: uname, fullName, role };
    setUser(userData);
    return userData;
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch (err) {
      // Log but don't block — user wants to log out regardless
      console.error('Logout API call failed:', err);
    } finally {
      clearSession();
    }
  }, [clearSession]);

  const value = { user, loading, login, register, logout };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
