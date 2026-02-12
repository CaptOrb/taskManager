import type { ReactElement } from "react";
import { useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import type { AuthProviderProps, DecodedToken } from "../types/auth";
import { AuthContext } from "./auth-context";

export const AuthProvider = ({ children }: AuthProviderProps): ReactElement => {
	const [loggedInUser, setLoggedInUser] = useState<string | null>(null);
	const [loading, setLoading] = useState(true);
	const navigate = useNavigate();
	const location = useLocation();

	// Memoized logout so it can safely be used in useEffect
	const logout = useCallback(() => {
		localStorage.removeItem("token");
		setLoggedInUser(null);
	}, []);

	const login = (token: string): void => {
		localStorage.setItem("token", token);
		const decodedToken = decodeToken(token);
		setLoggedInUser(decodedToken.displayName);
	};

	useEffect(() => {
		const checkToken = (): void => {
			const token = localStorage.getItem("token");
			if (!token) {
				logout();
				setLoading(false);
				return;
			}

			try {
				const decodedToken = decodeToken(token);
				if (decodedToken?.exp) {
					const expiration = decodedToken.exp * 1000;
					if (expiration < Date.now()) {
						logout();
						navigate(`/login?then=${encodeURIComponent(location.pathname)}`);
						return;
					}
					setLoggedInUser(decodedToken.displayName);
				} else {
					logout();
					navigate(`/login?then=${encodeURIComponent(location.pathname)}`);
				}
			} catch (error) {
				console.error("Error decoding token:", error);
				logout();
				navigate(`/login?then=${encodeURIComponent(location.pathname)}`);
			} finally {
				setLoading(false);
			}
		};

		checkToken();
		const intervalId = setInterval(checkToken, 60000);
		return (): void => clearInterval(intervalId);
	}, [navigate, logout, location.pathname]);

	return (
		<AuthContext.Provider value={{ loggedInUser, login, logout, loading }}>
			{children}
		</AuthContext.Provider>
	);
};

const decodeToken = (token: string): DecodedToken => {
	const payload = token.split(".")[1];
	if (!payload) {
		throw new Error("Invalid JWT");
	}

	return JSON.parse(atob(payload)) satisfies DecodedToken;
};
