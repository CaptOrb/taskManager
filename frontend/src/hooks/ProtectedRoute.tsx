import type { ReactElement, ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "./auth-context";

interface ProtectedRouteProps {
	children: ReactNode;
}

const ProtectedRoute = ({ children }: ProtectedRouteProps): ReactElement => {
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
