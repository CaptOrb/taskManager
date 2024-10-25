import React from 'react';
import { useAuth } from './AuthContext'; 
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
    const { loggedInUser, loading } = useAuth(); 

    if (loading) {
        return <div>Loading...</div>;
    }

    if (!loggedInUser) {
        return <Navigate to="/login" />;
    }

    return children;
};

export default ProtectedRoute;
