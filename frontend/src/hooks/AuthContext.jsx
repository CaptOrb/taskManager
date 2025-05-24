import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import axios from 'axios';

const AuthContext = createContext();

function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const padded = base64.padEnd(base64.length + (4 - base64.length % 4) % 4, '=');
        const jsonPayload = decodeURIComponent(
            atob(padded)
                .split('')
                .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch (e) {
        //console.error('[Auth] Failed to parse JWT:', e);
        return null;
    }
}

export const AuthProvider = ({ children }) => {
    const [loggedInUser, setLoggedInUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [accessToken, setAccessToken] = useState(null);
    const refreshTimeoutRef = useRef(null);

    const refreshJwtToken = async () => {
        try {
            const response = await axios.post('/api/auth/refresh-token', null, {
                withCredentials: true,
            });

            if (response.status !== 200) throw new Error('Token refresh failed');

            const newAccessToken = response.data.jwtToken;
            localStorage.setItem('accessToken', newAccessToken);
            setAccessToken(newAccessToken);

            const decoded = parseJwt(newAccessToken);
            if (!decoded || !decoded.sub) throw new Error('Invalid token');
            setLoggedInUser(decoded.sub);
            scheduleTokenRefresh(newAccessToken);
        } catch (error) {
            //console.error('[Auth] Token refresh failed:', error);
            logout();
        }
    };

    const scheduleTokenRefresh = (token) => {
        if (refreshTimeoutRef.current) clearTimeout(refreshTimeoutRef.current);

        try {
            const decoded = parseJwt(token);
            if (!decoded || !decoded.exp) throw new Error('Invalid token');
            const expiration = decoded.exp * 1000;
            const timeUntilRefresh = expiration - Date.now() - 60 * 1000;
            if (timeUntilRefresh > 0) {
                refreshTimeoutRef.current = setTimeout(() => {
                    refreshJwtToken();
                }, timeUntilRefresh);
            } else {
                refreshJwtToken();
            }
        } catch (err) {
            //console.error('[Auth] Failed to decode token in scheduler:', err);
            logout();
        }
    };

    const checkAndRefreshToken = async () => {
        const storedAccessToken = localStorage.getItem('accessToken');

        if (!storedAccessToken) {
            logout();
            setLoading(false);
            return;
        }

        try {
            const decoded = parseJwt(storedAccessToken);
            if (!decoded || !decoded.exp || !decoded.sub) throw new Error('Invalid token');
            const expiration = decoded.exp * 1000;
            const timeLeft = expiration - Date.now();

            if (timeLeft <= 0) {
                await refreshJwtToken();
            } else {
                setAccessToken(storedAccessToken);
                setLoggedInUser(decoded.sub);
                scheduleTokenRefresh(storedAccessToken);
            }
        } catch (err) {
            //console.error('[Auth] Token parsing failed during check:', err);
            logout();
        }

        setLoading(false);
    };

    useEffect(() => {
        checkAndRefreshToken();
        const interval = setInterval(checkAndRefreshToken, 5 * 60 * 1000);
        return () => {
            clearInterval(interval);
            if (refreshTimeoutRef.current) clearTimeout(refreshTimeoutRef.current);
        };
    }, []);

    const login = (newAccessToken) => {
        localStorage.setItem('accessToken', newAccessToken);
        setAccessToken(newAccessToken);

        const decoded = parseJwt(newAccessToken);
        if (!decoded || !decoded.sub) {
            //console.error('[Auth] Login failed due to invalid token.');
            logout();
            return;
        }

        //console.log('[Auth] Login successful for user:', decoded.sub);
        setLoggedInUser(decoded.sub);
        scheduleTokenRefresh(newAccessToken);
    };

    const logout = async () => {
        try {
            await axios.post('/api/auth/logout', null, { withCredentials: true });
            //console.log('[Auth] Server logout successful');
        } catch (error) {
            //console.error('[Auth] Server logout failed:', error);
        } finally {
            localStorage.removeItem('accessToken');
            setAccessToken(null);
            setLoggedInUser(null);
            if (refreshTimeoutRef.current) clearTimeout(refreshTimeoutRef.current);
        }
    };

    return (
        <AuthContext.Provider value={{ loggedInUser, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
