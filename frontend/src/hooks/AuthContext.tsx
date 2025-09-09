import type React from 'react';
import { createContext, useContext, useState, useEffect, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom';

interface AuthContextType {
    loggedInUser: string | null;
    login: (token: string) => void;
    logout: () => void;
    loading: boolean;
}

interface AuthProviderProps {
    children: ReactNode;
}

interface DecodedToken {
    sub: string;
    exp: number;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
    const [loggedInUser, setLoggedInUser] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const navigate = useNavigate();

    useEffect(() => {
        const checkToken = () => {
            const token = localStorage.getItem('token');
            if (token) {
                try {
                    const decodedToken: DecodedToken = JSON.parse(atob(token.split('.')[1]));
                    if (decodedToken?.exp) {
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

    const login = (token: string): void => {
        localStorage.setItem('token', token);
        const decodedToken: DecodedToken = JSON.parse(atob(token.split('.')[1]));
        setLoggedInUser(decodedToken.sub);
    };

    const logout = (): void => {
        localStorage.removeItem('token');
        setLoggedInUser(null);
    };

    return (
        <AuthContext.Provider value={{ loggedInUser, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};