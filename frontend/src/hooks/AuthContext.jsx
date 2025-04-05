import React, { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [loggedInUser, setLoggedInUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const checkToken = () => {
            const token = localStorage.getItem('token');
            if (token) {
                try {
                    const decodedToken = JSON.parse(atob(token.split('.')[1]));
                    if (decodedToken && decodedToken.exp) {
                        const expiration = decodedToken.exp * 1000;
                        if (expiration < Date.now()) {
                            logout();
                            navigate('/login');
                            return;
                        }
                        setLoggedInUser(decodedToken.sub);
                    } else {
                        logout();
                        navigate('/login');
                    }
                } catch (error) {
                    console.error('Error decoding token:', error);
                    logout();
                    navigate('/login');
                }
            }
            setLoading(false);
        };

        checkToken();
        const intervalId = setInterval(checkToken, 60000);

        return () => clearInterval(intervalId);
    }, [navigate]);

    const login = (token) => {
        localStorage.setItem('token', token);
        const decodedToken = JSON.parse(atob(token.split('.')[1]));
        setLoggedInUser(decodedToken.sub);
    };

    const logout = () => {
        localStorage.removeItem('token');
        setLoggedInUser(null);
    };

    return (
        <AuthContext.Provider value={{ loggedInUser, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    return useContext(AuthContext);
};