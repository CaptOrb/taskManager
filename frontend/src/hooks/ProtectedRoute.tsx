import type React from "react";
import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

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
