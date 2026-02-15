import { type ReactElement, type SubmitEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { LoginResponse } from "@/types/auth";
import { useAuth } from "../hooks/auth-context";
import api from "../utils/api";
import { getApiErrorMessage, getApiFieldErrors } from "../utils/apiError";

type RegisterField = "userName" | "email" | "password" | "passwordConfirm";

type RegisterFieldErrors = Record<RegisterField, string[]>;

const createEmptyFieldErrors = (): RegisterFieldErrors => ({
  userName: [],
  email: [],
  password: [],
  passwordConfirm: [],
});

const mapApiFieldErrorsToRegisterFields = (
  apiFieldErrors: Record<string, string[]>,
): RegisterFieldErrors => ({
  userName: apiFieldErrors["userName"] ?? [],
  email: apiFieldErrors["email"] ?? [],
  password: apiFieldErrors["password"] ?? [],
  passwordConfirm: apiFieldErrors["passwordConfirm"] ?? [],
});

const hasAnyFieldError = (fieldErrors: RegisterFieldErrors): boolean =>
  Object.values(fieldErrors).some(
    (errorsForField) => errorsForField.length > 0,
  );

const getInputClassName = (hasError: boolean): string =>
  `bg-gray-50 border text-gray-900 text-sm rounded-lg block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white ${hasError ? "border-red-500 focus:ring-red-500 focus:border-red-500 dark:focus:ring-red-500 dark:focus:border-red-500" : "border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:focus:ring-blue-500 dark:focus:border-blue-500"}`;

const Register = (): ReactElement => {
  const [userName, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [fieldErrors, setFieldErrors] = useState<RegisterFieldErrors>(() =>
    createEmptyFieldErrors(),
  );
  const [formError, setFormError] = useState("");
  const navigate = useNavigate();
  const { login } = useAuth();

  const clearFieldError = (field: RegisterField): void => {
    setFieldErrors((previousErrors) => ({
      ...previousErrors,
      [field]: [],
    }));
    setFormError("");
  };

  const handleRegister = async (
    e: SubmitEvent<HTMLFormElement>,
  ): Promise<void> => {
    e.preventDefault();
    setFormError("");
    setFieldErrors(createEmptyFieldErrors());

    try {
      const response = await api.post<LoginResponse>("/auth/register", {
        userName,
        email,
        password,
        passwordConfirm: confirmPassword,
      });

      const { jwtToken } = response.data;
      login(jwtToken);
      navigate("/");
    } catch (error) {
      const fieldLevelErrors = mapApiFieldErrorsToRegisterFields(
        getApiFieldErrors(error),
      );
      setFieldErrors(fieldLevelErrors);

      if (!hasAnyFieldError(fieldLevelErrors)) {
        setFormError(getApiErrorMessage(error, "Registration failed"));
      }

      console.error("Registration failed:", error);
    }
  };

  return (
    <div className="max-w-sm mx-auto mt-8">
      <form
        onSubmit={handleRegister}
        className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4 dark:bg-gray-800"
      >
        <div className="mb-4">
          {fieldErrors.userName.map((fieldError) => (
            <p
              key={`userName-${fieldError}`}
              className="text-red-500 text-sm mb-1"
            >
              {fieldError}
            </p>
          ))}
          <input
            type="text"
            placeholder="Username"
            value={userName}
            onChange={(e) => {
              setUsername(e.target.value);
              clearFieldError("userName");
            }}
            aria-invalid={fieldErrors.userName.length > 0}
            required
            className={getInputClassName(fieldErrors.userName.length > 0)}
          />
        </div>

        <div className="mb-4">
          {fieldErrors.email.map((fieldError) => (
            <p
              key={`email-${fieldError}`}
              className="text-red-500 text-sm mb-1"
            >
              {fieldError}
            </p>
          ))}
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              clearFieldError("email");
            }}
            aria-invalid={fieldErrors.email.length > 0}
            required
            className={getInputClassName(fieldErrors.email.length > 0)}
          />
        </div>

        <div className="mb-4">
          {fieldErrors.password.map((fieldError) => (
            <p
              key={`password-${fieldError}`}
              className="text-red-500 text-sm mb-1"
            >
              {fieldError}
            </p>
          ))}
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              clearFieldError("password");
            }}
            aria-invalid={fieldErrors.password.length > 0}
            required
            className={getInputClassName(fieldErrors.password.length > 0)}
          />
        </div>

        <div className="mb-4">
          {fieldErrors.passwordConfirm.map((fieldError) => (
            <p
              key={`passwordConfirm-${fieldError}`}
              className="text-red-500 text-sm mb-1"
            >
              {fieldError}
            </p>
          ))}
          <input
            type="password"
            placeholder="Confirm Password"
            value={confirmPassword}
            onChange={(e) => {
              setConfirmPassword(e.target.value);
              clearFieldError("passwordConfirm");
            }}
            aria-invalid={fieldErrors.passwordConfirm.length > 0}
            required
            className={getInputClassName(
              fieldErrors.passwordConfirm.length > 0,
            )}
          />
        </div>

        {formError && <p className="text-red-500 mb-4">{formError}</p>}

        <button
          type="submit"
          className="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm w-full px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
        >
          Register
        </button>
      </form>
    </div>
  );
};

export default Register;
