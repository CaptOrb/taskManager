import {
  type ChangeEvent,
  type FormEvent,
  type ReactElement,
  useEffect,
  useId,
  useState,
} from "react";
import { useNavigate } from "react-router-dom";
import {
  getApiErrorMessage,
  getApiErrorMessageFromBody,
  getApiFieldErrorsFromBody,
} from "../utils/apiError";

type PasswordField = "currentPassword" | "newPassword" | "confirmPassword";

type PasswordForm = Record<PasswordField, string>;

type PasswordFieldErrors = Record<PasswordField, string[]>;

const createEmptyPasswordFieldErrors = (): PasswordFieldErrors => ({
  currentPassword: [],
  newPassword: [],
  confirmPassword: [],
});

const mapApiFieldErrorsToPasswordFields = (
  apiFieldErrors: Record<string, string[]>,
): PasswordFieldErrors => ({
  currentPassword: apiFieldErrors["currentPassword"] ?? [],
  newPassword: apiFieldErrors["newPassword"] ?? [],
  confirmPassword: apiFieldErrors["confirmPassword"] ?? [],
});

const hasAnyFieldError = (fieldErrors: PasswordFieldErrors): boolean =>
  Object.values(fieldErrors).some(
    (errorsForField) => errorsForField.length > 0,
  );

const isPasswordField = (field: string): field is PasswordField =>
  field === "currentPassword" ||
  field === "newPassword" ||
  field === "confirmPassword";

const getInputClassName = (hasError: boolean): string =>
  `bg-gray-50 border text-gray-900 text-sm rounded-lg focus:ring-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white ${hasError ? "border-red-500 focus:border-red-500 dark:focus:ring-red-500 dark:focus:border-red-500" : "border-gray-300 focus:border-blue-500 dark:focus:ring-blue-500 dark:focus:border-blue-500"}`;

const MyAccount = (): ReactElement => {
  const [userName, setUserName] = useState("");
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showPasswordChange, setShowPasswordChange] = useState<boolean>(false);
  const [passwordChangeLoading, setPasswordChangeLoading] =
    useState<boolean>(false);
  const [passwordChangeError, setPasswordChangeError] = useState<string | null>(
    null,
  );
  const [passwordChangeSuccess, setPasswordChangeSuccess] =
    useState<boolean>(false);
  const [passwordForm, setPasswordForm] = useState<PasswordForm>({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [passwordFieldErrors, setPasswordFieldErrors] =
    useState<PasswordFieldErrors>(createEmptyPasswordFieldErrors);
  const navigate = useNavigate();

  const userNameId = useId();
  const emailId = useId();
  const currentPasswordId = useId();
  const newPasswordId = useId();
  const confirmPasswordId = useId();

  useEffect(() => {
    const fetchCurrentUser = async (): Promise<void> => {
      try {
        const response = await fetch("/api/auth/current-user", {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });

        if (!response.ok) {
          const data: unknown = await response.json().catch(() => null);
          throw new Error(
            getApiErrorMessageFromBody(data, "Failed to fetch user"),
          );
        }

        const data: unknown = await response.json();
        if (typeof data === "object" && data !== null) {
          const user = data as { userName?: string; email?: string };
          setUserName(user.userName ?? "");
          setEmail(user.email ?? "");
        }
      } catch (error) {
        setError(getApiErrorMessage(error, "Failed to fetch user"));
      } finally {
        setLoading(false);
      }
    };

    fetchCurrentUser();
  }, []);

  const handlePasswordChange = async (
    e: FormEvent<HTMLFormElement>,
  ): Promise<void> => {
    e.preventDefault();
    setPasswordChangeLoading(true);
    setPasswordChangeError(null);
    setPasswordChangeSuccess(false);
    setPasswordFieldErrors(createEmptyPasswordFieldErrors());

    try {
      const response = await fetch("/api/auth/change-password", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify(passwordForm),
      });

      if (!response.ok) {
        const data: unknown = await response.json().catch(() => null);
        const fieldLevelErrors = mapApiFieldErrorsToPasswordFields(
          getApiFieldErrorsFromBody(data),
        );
        setPasswordFieldErrors(fieldLevelErrors);

        if (!hasAnyFieldError(fieldLevelErrors)) {
          setPasswordChangeError(
            getApiErrorMessageFromBody(data, "Failed to change password"),
          );
        }

        return;
      }

      setPasswordChangeSuccess(true);
      setPasswordForm({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
      setPasswordFieldErrors(createEmptyPasswordFieldErrors());
      setShowPasswordChange(false);

      // Clear success message after 3 seconds
      setTimeout(() => {
        setPasswordChangeSuccess(false);
      }, 3000);
    } catch (error) {
      setPasswordChangeError(
        getApiErrorMessage(error, "Failed to change password"),
      );
    } finally {
      setPasswordChangeLoading(false);
    }
  };

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target;
    if (!isPasswordField(name)) {
      return;
    }

    setPasswordForm((prev) => ({
      ...prev,
      [name]: value,
    }));
    setPasswordFieldErrors((prev) => ({
      ...prev,
      [name]: [],
    }));
    setPasswordChangeError(null);
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <div className="max-w-sm mx-auto mt-8">
      <div className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4 dark:bg-gray-800">
        <label
          htmlFor={userNameId}
          className="block text-sm font-medium text-gray-700 dark:text-white"
        >
          Username
        </label>
        <input
          type="text"
          id={userNameId}
          value={userName}
          disabled
          aria-label="Username"
          className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
        />

        <label
          htmlFor={emailId}
          className="block text-sm font-medium text-gray-700 dark:text-white"
        >
          Email
        </label>
        <input
          type="email"
          id={emailId}
          value={email}
          disabled
          aria-label="Email"
          className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
        />

        {passwordChangeSuccess && (
          <div className="mb-4 p-4 text-sm text-green-800 border border-green-300 rounded-lg bg-green-50 dark:bg-gray-800 dark:text-green-400 dark:border-green-800">
            Password changed successfully!
          </div>
        )}

        {!showPasswordChange ? (
          <button
            type="button"
            onClick={() => {
              setShowPasswordChange(true);
              setPasswordChangeError(null);
              setPasswordFieldErrors(createEmptyPasswordFieldErrors());
            }}
            className="text-white bg-blue-700 hover:bg-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 mb-2 mr-2"
          >
            Change Password
          </button>
        ) : (
          <div className="mb-4 p-4 border border-gray-300 rounded-lg bg-gray-50 dark:bg-gray-700 dark:border-gray-600">
            <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">
              Change Password
            </h3>

            {passwordChangeError && (
              <div className="mb-4 p-4 text-sm text-red-800 border border-red-300 rounded-lg bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800">
                {passwordChangeError}
              </div>
            )}

            <form onSubmit={handlePasswordChange} method="POST">
              {passwordFieldErrors.currentPassword.map((fieldError) => (
                <p
                  key={`currentPassword-${fieldError}`}
                  className="text-red-500 text-sm mb-1"
                >
                  {fieldError}
                </p>
              ))}
              <label
                htmlFor={currentPasswordId}
                className="block text-sm font-medium text-gray-700 dark:text-white"
              >
                Current Password
              </label>
              <input
                type="password"
                id={currentPasswordId}
                name="currentPassword"
                value={passwordForm.currentPassword}
                onChange={handleInputChange}
                aria-invalid={passwordFieldErrors.currentPassword.length > 0}
                required
                className={getInputClassName(
                  passwordFieldErrors.currentPassword.length > 0,
                )}
              />

              {passwordFieldErrors.newPassword.map((fieldError) => (
                <p
                  key={`newPassword-${fieldError}`}
                  className="text-red-500 text-sm mb-1"
                >
                  {fieldError}
                </p>
              ))}
              <label
                htmlFor={newPasswordId}
                className="block text-sm font-medium text-gray-700 dark:text-white"
              >
                New Password
              </label>
              <input
                type="password"
                id={newPasswordId}
                name="newPassword"
                value={passwordForm.newPassword}
                onChange={handleInputChange}
                aria-invalid={passwordFieldErrors.newPassword.length > 0}
                required
                className={getInputClassName(
                  passwordFieldErrors.newPassword.length > 0,
                )}
              />

              {passwordFieldErrors.confirmPassword.map((fieldError) => (
                <p
                  key={`confirmPassword-${fieldError}`}
                  className="text-red-500 text-sm mb-1"
                >
                  {fieldError}
                </p>
              ))}
              <label
                htmlFor={confirmPasswordId}
                className="block text-sm font-medium text-gray-700 dark:text-white"
              >
                Confirm New Password
              </label>
              <input
                type="password"
                id={confirmPasswordId}
                name="confirmPassword"
                value={passwordForm.confirmPassword}
                onChange={handleInputChange}
                aria-invalid={passwordFieldErrors.confirmPassword.length > 0}
                required
                className={getInputClassName(
                  passwordFieldErrors.confirmPassword.length > 0,
                )}
              />

              <div className="flex gap-2">
                <button
                  type="submit"
                  disabled={passwordChangeLoading}
                  className="text-white bg-blue-700 hover:bg-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 mb-2 disabled:opacity-50"
                >
                  {passwordChangeLoading ? "Changing..." : "Change Password"}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowPasswordChange(false);
                    setPasswordForm({
                      currentPassword: "",
                      newPassword: "",
                      confirmPassword: "",
                    });
                    setPasswordFieldErrors(createEmptyPasswordFieldErrors());
                    setPasswordChangeError(null);
                  }}
                  className="text-white bg-gray-700 hover:bg-gray-800 font-medium rounded-lg text-sm px-5 py-2.5 mb-2"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}

        <button
          type="button"
          onClick={() => {
            if (window.history.length > 1) {
              navigate(-1);
            } else {
              navigate("/");
            }
          }}
          className="text-white bg-red-700 hover:bg-red-800 font-medium rounded-lg text-sm px-5 py-2.5 mb-2"
        >
          Cancel
        </button>
      </div>
    </div>
  );
};

export default MyAccount;
