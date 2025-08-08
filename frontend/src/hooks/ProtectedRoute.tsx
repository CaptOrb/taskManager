import React, { ReactNode } from 'react';
import { useAuth } from './AuthContext'; 
import { Navigate } from 'react-router-dom';

interface ProtectedRouteProps {
    children: ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
    const { loggedInUser, loading } = useAuth(); 

    if (loading) {
        return <div>Loading...</div>;
    }

    if (!loggedInUser) {
        return <Navigate to="/login" />;
    }

    return <>{children}</>;
};

export default ProtectedRoute;
