import type { ReactNode } from "react";

export interface LoginRequest {
	userName: string;
	password: string;
}

export interface LoginResponse {
	jwtToken: string;
}

export interface RegisterRequest {
	userName: string;
	email: string;
	password: string;
}

export interface PasswordChangeRequest {
	currentPassword: string;
	newPassword: string;
}

export interface ApiError {
	message: string;
	status: number;
}

export interface AuthContextType {
	loggedInUser: string | null;
	login: (token: string) => void;
	logout: () => void;
	loading: boolean;
}

export interface AuthProviderProps {
	children: ReactNode;
}

export interface DecodedToken {
	sub: string;
	displayName: string;
	exp: number;
}
