// see https://www.reddit.com/r/react/comments/1k2tgid/how_is_this_done_in_real_life_work_react_context/
import { createContext, useContext } from "react";
import type { AuthContextType } from "../types/auth";

export const AuthContext = createContext<AuthContextType | undefined>(
	undefined,
);

export const useAuth = (): AuthContextType => {
	const context = useContext(AuthContext);
	if (context === undefined) {
		throw new Error("useAuth must be used within an AuthProvider");
	}
	return context;
};
