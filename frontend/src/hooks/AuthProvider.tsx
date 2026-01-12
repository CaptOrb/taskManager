import type { ReactElement } from "react";
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { AuthProviderProps, DecodedToken } from "../types/auth";
import { AuthContext } from "./auth-context";

export const AuthProvider = ({ children }: AuthProviderProps): ReactElement => {
	const [loggedInUser, setLoggedInUser] = useState<string | null>(null);
	const [loading, setLoading] = useState(true);
	const navigate = useNavigate();

	// Memoized logout so it can safely be used in useEffect
	const logout = useCallback(() => {
		localStorage.removeItem("token");
		setLoggedInUser(null);
	}, []);

	const login = (token: string): void => {
		localStorage.setItem("token", token);
		const decodedToken = decodeToken(token);
		setLoggedInUser(decodedToken.sub);
	};

	useEffect(() => {
		const checkToken = (): void => {
			const token = localStorage.getItem("token");
			if (token) {
				try {
					const decodedToken = decodeToken(token);
					if (decodedToken?.exp) {
						const expiration = decodedToken.exp * 1000;
						if (expiration < Date.now()) {
							logout();
							navigate("/login");
							return;
						}
						setLoggedInUser(decodedToken.sub);
					} else {
						logout();
						navigate("/login");
					}
				} catch (error) {
					console.log("Error decoding token:", error);
					logout();
					navigate("/login");
				}
			}
			setLoading(false);
		};

		checkToken();
		const intervalId = setInterval(checkToken, 60000);
		return (): void => clearInterval(intervalId);
	}, [navigate, logout]);

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
