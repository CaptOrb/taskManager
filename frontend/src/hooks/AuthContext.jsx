import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [loggedInUser, setLoggedInUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [accessToken, setAccessToken] = useState(null);
    const [refreshToken, setRefreshToken] = useState(null);

    const refreshJwtToken = async (oldRefreshToken) => {
        try {
            const response = await fetch('/api/auth/refresh-token', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${oldRefreshToken}`,
                },
            });

            if (!response.ok) throw new Error('Token refresh failed');

            const data = await response.json();
            return { accessToken: data.accessToken, refreshToken: data.refreshToken };
        } catch (error) {
            console.error('Token refresh failed:', error);
            return null;
        }
    };

    const checkAndRefreshToken = async () => {
        const storedAccessToken = localStorage.getItem('accessToken');
        const storedRefreshToken = localStorage.getItem('refreshToken');

        if (!storedAccessToken || !storedRefreshToken) {
            logout();
            setLoading(false);
            return;
        }

        // If accessToken is available, check if it's still valid
        try {
            const decodedToken = JSON.parse(atob(storedAccessToken.split('.')[1]));
            const expiration = decodedToken.exp * 1000;
            const timeLeft = expiration - Date.now();

            if (timeLeft <= 0) {
                // Access token has expired, try refreshing it with refresh token
                const newTokens = await refreshJwtToken(storedRefreshToken);
                if (newTokens) {
                    setAccessToken(newTokens.accessToken);
                    setRefreshToken(newTokens.refreshToken);
                    localStorage.setItem('accessToken', newTokens.accessToken);
                    localStorage.setItem('refreshToken', newTokens.refreshToken);
                    const newDecoded = JSON.parse(atob(newTokens.accessToken.split('.')[1]));
                    setLoggedInUser(newDecoded.sub);
                } else {
                    logout();
                }
            } else {
                // Access token is still valid, just update user state
                setLoggedInUser(decodedToken.sub);
            }
        } catch (err) {
            console.error('Error checking token:', err);
            logout();
        }

        setLoading(false);
    };

    useEffect(() => {
        checkAndRefreshToken();
        const interval = setInterval(checkAndRefreshToken, 5 * 60 * 1000); // Check every 5 minutes
        return () => clearInterval(interval);
    }, []); 
    const login = (accessToken, refreshToken) => {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);

        setAccessToken(accessToken);
        setRefreshToken(refreshToken);

        const decoded = JSON.parse(atob(accessToken.split('.')[1]));
        setLoggedInUser(decoded.sub);

        checkAndRefreshToken();
    };

    const logout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        setLoggedInUser(null);
        setAccessToken(null);
        setRefreshToken(null);
    };

    return (
        <AuthContext.Provider value={{ loggedInUser, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
