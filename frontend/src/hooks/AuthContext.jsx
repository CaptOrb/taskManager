import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [loggedInUser, setLoggedInUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [accessToken, setAccessToken] = useState(null);

    const refreshJwtToken = async () => {
        try {
            const response = await axios.post('/api/auth/refresh-token', null, {
                withCredentials: true,  // Send the refresh token stored in the HTTP-only cookie
            });
    
            if (response.status !== 200) {
                throw new Error('Token refresh failed');
            }
    
            return response.data.accessToken;
        } catch (error) {
            console.error('Token refresh failed:', error);
            return null;
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
                const newAccessToken = await refreshJwtToken();
                if (newAccessToken) {
                    localStorage.setItem('accessToken', newAccessToken);
                    setAccessToken(newAccessToken);
                    const newDecoded = JSON.parse(atob(newAccessToken.split('.')[1]));
                    setLoggedInUser(newDecoded.sub);
                } else {
                    logout();
                }
            } else {
                setAccessToken(storedAccessToken);
                setLoggedInUser(decoded.sub);
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
        return () => clearInterval(interval);
    }, []);

    const login = (accessToken) => {
        localStorage.setItem('accessToken', accessToken);
        setAccessToken(accessToken);

        const decoded = JSON.parse(atob(accessToken.split('.')[1]));
        setLoggedInUser(decoded.sub);

        checkAndRefreshToken();
    };

    const logout = () => {
        localStorage.removeItem('accessToken');
        setAccessToken(null);
        setLoggedInUser(null);
    };

    return (
        <AuthContext.Provider value={{ loggedInUser, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
