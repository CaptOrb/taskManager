import {
  type SubmitEvent,
  type ReactElement,
  useEffect,
  useId,
  useState,
} from "react";
import {
  Link,
  useLocation,
  useNavigate,
  useSearchParams,
} from "react-router-dom";
import type { LoginResponse } from "@/types/auth";
import { useAuth } from "../hooks/auth-context";
import api from "../utils/api";
import { getApiErrorMessage } from "../utils/apiError";
import { getValidRedirectPath } from "../utils/redirect";

function Login(): ReactElement {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const usernameId = useId();
  const passwordId = useId();
  const [errorMessage, setErrorMessage] = useState<string>("");
  const { login, loggedInUser } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const redirectTo = getValidRedirectPath(searchParams.get("then"));

  useEffect(() => {
    if (loggedInUser) {
      navigate(redirectTo);
    }
  }, [loggedInUser, navigate, redirectTo]);

  const location = useLocation();
  const query = new URLSearchParams(location.search);
  const successMessage = query.get("success");

  const handleLogin = async (
    e: SubmitEvent<HTMLFormElement>,
  ): Promise<void> => {
    e.preventDefault();

    try {
      const response = await api.post<LoginResponse>("/auth/login", {
        userName: username,
        password: password,
      });

      const { jwtToken } = response.data;
      login(jwtToken);
      setErrorMessage("");
      navigate(redirectTo);
    } catch (error) {
      console.error("Login failed", error);
      setErrorMessage(
        getApiErrorMessage(error, "Login failed. Check your credentials."),
      );
    }
  };

  return (
    <div>
      <form onSubmit={handleLogin} className="max-w-sm mx-auto">
        {successMessage && (
          <p style={{ color: "green" }}>
            Registration complete! You can now log in.
          </p>
        )}
        <div className="mb-5">
          <label
            htmlFor={usernameId}
            className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
          >
            Username or Email Address
          </label>
          <input
            type="text"
            id={usernameId}
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
            placeholder="Enter your username or email"
            required
          />

          <label
            htmlFor={passwordId}
            className="block mt-2 mb-2 text-sm font-medium text-gray-900 dark:text-white"
          >
            Password
          </label>
          <input
            type="password"
            id={passwordId}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
            required
          />
        </div>

        <button
          type="submit"
          className="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm w-full sm:w-auto px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
        >
          Login
        </button>
        {errorMessage && <p style={{ color: "red" }}>{errorMessage}</p>}

        <p className="text-sm font-light text-gray-500 dark:text-gray-400">
          Don’t have an account yet?{" "}
          <Link
            to="/register"
            className="font-medium text-primary-600 hover:underline dark:text-primary-500"
          >
            Sign up
          </Link>
        </p>
      </form>
    </div>
  );
}

export default Login;
