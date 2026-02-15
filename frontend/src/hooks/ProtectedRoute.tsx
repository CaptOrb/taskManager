import type { ReactElement, ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "./auth-context";

interface ProtectedRouteProps {
  children: ReactNode;
}

const ProtectedRoute = ({ children }: ProtectedRouteProps): ReactElement => {
  const { loggedInUser, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!loggedInUser) {
    return (
      <Navigate to={`/login?then=${encodeURIComponent(location.pathname)}`} />
    );
  }

  return <>{children}</>;
};

export default ProtectedRoute;
