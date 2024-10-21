import React from 'react';
import { useAuth } from './AuthContext'; 
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
    const { loggedInUser } = useAuth(); 

    if (!loggedInUser) {
        return <Navigate to="/login" />;
    }

    // If authenticated, render the children (the protected component)
    return children;
};

export default ProtectedRoute;
