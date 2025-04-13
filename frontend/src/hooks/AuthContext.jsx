import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import axios from 'axios';

const AuthContext = createContext();

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
            
            const newAccessToken = response.data.accessToken;
            localStorage.setItem('accessToken', newAccessToken);
            setAccessToken(newAccessToken);
            const decoded = JSON.parse(atob(newAccessToken.split('.')[1]));
            setLoggedInUser(decoded.sub);
            scheduleTokenRefresh(newAccessToken);
        } catch (error) {
            console.error('Token refresh failed:', error);
            logout();
        }
    };

    const scheduleTokenRefresh = (token) => {
        if (refreshTimeoutRef.current) clearTimeout(refreshTimeoutRef.current);

        try {
            const decoded = JSON.parse(atob(token.split('.')[1]));
            const expiration = decoded.exp * 1000;
            const timeUntilRefresh = expiration - Date.now() - 60 * 1000;
            if (timeUntilRefresh > 0) {
                refreshTimeoutRef.current = setTimeout(() => {
                    refreshJwtToken();
                }, timeUntilRefresh);
            } else {
                refreshJwtToken(); // token is already close to being expired
            }
        } catch (err) {
            console.error('Failed to decode token:', err);
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
            const decoded = JSON.parse(atob(storedAccessToken.split('.')[1]));
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
            console.error('Token parsing failed:', err);
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

        const decoded = JSON.parse(atob(newAccessToken.split('.')[1]));
        setLoggedInUser(decoded.sub);

        scheduleTokenRefresh(newAccessToken);
    };

    const logout = () => {
        localStorage.removeItem('accessToken');
        setAccessToken(null);
        setLoggedInUser(null);
        if (refreshTimeoutRef.current) clearTimeout(refreshTimeoutRef.current);
    };

    return (
        <AuthContext.Provider value={{ loggedInUser, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
